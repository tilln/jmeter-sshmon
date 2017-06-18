package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import kg.apc.emulators.TestJMeterUtils;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SSHSessionFactoryIT {
    public static ConnectionDetails localConnection = new ConnectionDetails("0.0.0.0", Integer.valueOf(System.getProperty("sshmon.sshd.port")));
    public SSHSessionFactory instance;
    public Session session;

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
    public void tearDown() {
        if (session != null && session.isConnected()) {
            session.disconnect();
            session = null;
        }
    }

    @Test
    public void testWrap() throws Exception {
        session = new JSch().getSession(""); // dummy
        PooledObject<Session> actual = instance.wrap(session);
        assertTrue(actual instanceof DefaultPooledObject);
        assertEquals(session, actual.getObject());
    }

    @Test
    public void testCreateNoHostValidation() throws Exception {
        session = instance.create(localConnection);
        assertTrue(session.isConnected());
    }

    @Test(expected=JSchException.class)
    public void testCreateFailedHostValidation() throws Exception {
        JMeterUtils.setProperty("jmeter.sshmon.knownHosts", "src/test/resources/failed_known_hosts");
        instance = new SSHSessionFactory();
        session = instance.create(localConnection);
        assertFalse(session.isConnected());
    }

    @Test
    public void testCreateSuccessfulHostValidation() throws Exception {
        JMeterUtils.setProperty("jmeter.sshmon.knownHosts", "src/test/resources/known_hosts");
        instance = new SSHSessionFactory();
        session = instance.create(localConnection);
        assertTrue(session.isConnected());
    }

    @Test
    public void testDestroyObject() throws Exception {
        session = instance.create(localConnection);
        instance.destroyObject(localConnection, instance.wrap(session));
        assertFalse(session.isConnected());
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
