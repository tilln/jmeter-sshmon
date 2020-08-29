package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.config.hosts.ConfigFileHostEntryResolver;
import org.apache.sshd.client.config.hosts.HostConfigEntryResolver;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.config.keys.FilePasswordProvider;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.security.KeyPair;

/**
 * Factory class that manages SSH sessions for Apache Commons connection pool.
 */
public class SSHSessionFactory extends BaseKeyedPooledObjectFactory<ConnectionDetails, ClientSession> {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final SshClient sshc;

    public SSHSessionFactory() {
        sshc = SshClient.setUpDefaultClient();
        // for plugin backward compatibility, ignore known hosts and ssh config (unless explicitly configured):
        sshc.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);
        sshc.setHostConfigEntryResolver(HostConfigEntryResolver.EMPTY);

        String sshConfig = JMeterUtils.getProperty("jmeter.sshmon.sshConfig");
        if (sshConfig != null && !sshConfig.isEmpty()) {
            log.debug("ssh config file set to "+sshConfig);
            ConfigFileHostEntryResolver configResolver = new ConfigFileHostEntryResolver(Paths.get(sshConfig));
            sshc.setHostConfigEntryResolver(configResolver);
        }
        /* sshConfig may reference encrypted keys that require a password;
         * however configuring this via filename/password pairs is rather over the top, so assuming the common
         * use case of a single user with one identity file, a single password can be provided: */
        String identityPassword = JMeterUtils.getProperty("jmeter.sshmon.identityPassword");
        if (identityPassword != null && !identityPassword.isEmpty()) {
            log.debug("identity file password set to "+identityPassword);
            sshc.setFilePasswordProvider(FilePasswordProvider.of(identityPassword));
        }
        String knownHosts = JMeterUtils.getProperty("jmeter.sshmon.knownHosts");
        if (knownHosts != null && !knownHosts.isEmpty()) {
            log.debug("known hosts file set to "+knownHosts);
            sshc.setServerKeyVerifier(new KnownHostsServerKeyVerifier(RejectAllServerKeyVerifier.INSTANCE,
                Paths.get(new File(knownHosts).getPath())));
        }
        sshc.start();
    }

    public void stopClient() {
        sshc.stop();
    }

    @Override
    public ClientSession create(ConnectionDetails connectionDetails) throws Exception {
        log.debug("Creating session for "+connectionDetails);
        ClientSession session = null;
        try {
            session = sshc.connect(connectionDetails.getUsername(), connectionDetails.getHost(), connectionDetails.getPort())
                    .verify()
                    .getClientSession();

            String privateKey = connectionDetails.getPrivateKey();
            String password = connectionDetails.getPassword();

            if (privateKey != null && !privateKey.isEmpty()) {
                KeyPair keyPair = KeyHelper.toKeyPair(privateKey, password);
                session.addPublicKeyIdentity(keyPair);
            } else {
                if (password != null && !password.isEmpty()) {
                    session.addPasswordIdentity(password);
                }
            }
            session.auth().verify();
        } catch (Exception e) {
            log.error("Failed to connect to "+connectionDetails);
            throw e;
        }
        return session;
    }

    @Override
    public PooledObject<ClientSession> wrap(ClientSession session) {
        return new DefaultPooledObject<>(session);
    }

    @Override
    public void destroyObject(ConnectionDetails connectionDetails, PooledObject<ClientSession> sessionObject) {
        log.debug("Destroying session for "+connectionDetails);
        if (sessionObject != null) {
            ClientSession session = sessionObject.getObject();
            if (session != null) {
                try {
                    session.close();
                } catch (IOException e) {
                    log.error("Failed to close connection "+connectionDetails);
                }
            }
        }
    }

    @Override
    public boolean validateObject(ConnectionDetails connectionDetails, PooledObject<ClientSession> sessionObject) {
        log.debug("Validating session for "+connectionDetails);
        ClientSession session = (sessionObject == null) ? null : sessionObject.getObject();
        return session != null && session.isAuthenticated();
    }
}
