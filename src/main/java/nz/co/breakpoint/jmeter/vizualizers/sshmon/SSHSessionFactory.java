package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.commons.pool2.BaseKeyedPooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Factory class that manages SSH sessions for Apache Commons connection pool.
 */
public class SSHSessionFactory extends BaseKeyedPooledObjectFactory<ConnectionDetails, Session> {

    private static final Logger log = LoggingManager.getLoggerForClass();

    private final JSch jsch;

    public SSHSessionFactory() {
        JSch.setLogger(new JSchLogger());
        jsch = new JSch();

        // Change default from "ask" to avoid interactive confirmation:
        JSch.setConfig("StrictHostKeyChecking", "no");

        String knownHosts = JMeterUtils.getProperty("jmeter.sshmon.knownHosts");
        if (knownHosts != null && !knownHosts.isEmpty()) {
            try {
                log.debug("known hosts file set to "+knownHosts);
                jsch.setKnownHosts(knownHosts);
                JSch.setConfig("StrictHostKeyChecking", "yes");
            }
            catch (JSchException e) {
                log.error("Failed to set known hosts ", e);
            }
        }
    }

    @Override
    public Session create(ConnectionDetails connectionDetails) throws Exception {
        log.debug("Creating session for "+connectionDetails);
        Session session = null;
        try {
            byte[] privateKey = connectionDetails.getPrivateKey();
            if (privateKey != null) {
                jsch.addIdentity(connectionDetails.getUsername(), privateKey, null, connectionDetails.getPassword().getBytes());
            }
            session = jsch.getSession(connectionDetails.getUsername(), connectionDetails.getHost(), connectionDetails.getPort());
            session.setPassword(connectionDetails.getPassword());
            session.setServerAliveCountMax(Integer.MAX_VALUE); // change from default value 1 to prevent disconnects
            session.setDaemonThread(true);
            session.connect();
        } catch (Exception e) {
            log.error("Failed to connect to "+connectionDetails);
            throw e;
        }
        return session;
    }

    @Override
    public PooledObject<Session> wrap(Session session) {
        return new DefaultPooledObject<Session>(session);
    }

    @Override
    public void destroyObject(ConnectionDetails connectionDetails, PooledObject<Session> sessionObject) {
        log.debug("Destroying session for "+connectionDetails);
        if (sessionObject != null) {
            Session session = sessionObject.getObject();
            if (session != null) {
                session.disconnect();
            }
        }
    }

    @Override
    public boolean validateObject(ConnectionDetails connectionDetails, PooledObject<Session> sessionObject) {
        log.debug("Validating session for "+connectionDetails);
        Session session = (sessionObject == null) ? null : sessionObject.getObject();
        return session != null && session.isConnected();
    }
}
