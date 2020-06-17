package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.keyverifier.KnownHostsServerKeyVerifier;
import org.apache.sshd.client.keyverifier.RejectAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;

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
        sshc.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE);

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

            byte[] privateKey = connectionDetails.getPrivateKey();
            String password = connectionDetails.getPassword();

            if (privateKey != null) {
                KeyPair keyPair = KeyHelper.pemToKeyPair(privateKey, password);
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
