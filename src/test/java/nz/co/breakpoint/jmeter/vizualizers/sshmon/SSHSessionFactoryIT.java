package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.sshd.client.future.AuthFuture;
import org.apache.sshd.client.session.AbstractClientSession;
import org.apache.sshd.client.session.ClientSessionImpl;
import org.apache.sshd.common.SshException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.apache.sshd.client.session.ClientSession;

public class SSHSessionFactoryIT {
    public static ConnectionDetails localConnection = new ConnectionDetails("dummy",
        "localhost", Integer.valueOf(System.getProperty("sshmon.sshd.port")), "dummy");
    public SSHSessionFactory instance;
    public ClientSession session;

    @BeforeClass
    public static void setUpClass() {
        TestJMeterUtils.createJmeterEnv();
    }

    @Before
    public void setUp() {
        JMeterUtils.setProperty("jmeter.sshmon.knownHosts", ""); // make sure validation is off for most tests
        instance = new SSHSessionFactory();
    }

    @After
    public void tearDown() throws Exception {
        if (session != null && !session.isClosed()) {
            session.close();
            session = null;
        }
    }

    @Test
    public void testCreateNoHostValidation() throws Exception {
        session = instance.create(localConnection);
        assertTrue(session.isAuthenticated());
    }

    @Test(expected=SshException.class)
    public void testCreateFailedHostValidation() throws Exception {
        JMeterUtils.setProperty("jmeter.sshmon.knownHosts", "src/test/resources/failed_known_hosts");
        instance = new SSHSessionFactory();
        session = instance.create(localConnection);
        assertFalse(session.isAuthenticated());
    }

    @Test
    public void testCreateSuccessfulHostValidation() throws Exception {
        JMeterUtils.setProperty("jmeter.sshmon.knownHosts", "src/test/resources/known_hosts");
        instance = new SSHSessionFactory();
        session = instance.create(localConnection);
        assertTrue(session.isAuthenticated());
    }

    @Test
    public void testDestroyObject() throws Exception {
        session = instance.create(localConnection);
        instance.destroyObject(localConnection, instance.wrap(session));
        assertFalse(session.isOpen());
    }

    @Test
    public void testValidateObjectTrue() throws Exception {
        session = instance.create(localConnection);
        assertTrue(instance.validateObject(localConnection, instance.wrap(session)));
    }

    @Test
    public void testValidateObjectFalse() throws Exception {
        session = null;
        assertFalse(instance.validateObject(localConnection, instance.wrap(session)));
    }
}
