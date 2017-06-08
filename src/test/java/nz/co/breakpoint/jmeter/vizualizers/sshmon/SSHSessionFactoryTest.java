package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHSessionFactoryTest {
    public static SSHServerResource sshServer = new SSHServerResource(2222);
    public static ConnectionDetails localConnection = new ConnectionDetails("127.0.0.1", sshServer.getPort());
    public SSHSessionFactory instance;
    public Session session;

	@BeforeClass
    public static void setUpClass() {
        sshServer.start();
    }

    @AfterClass
    public static void tearDownClass() {
        sshServer.stop();
    }

    @Before
    public void setUp() {
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
    public void testCreate() throws Exception {
        System.out.println("create");
        session = instance.create(localConnection);
        assertTrue(session.isConnected());
    }

    @Test
    public void testDestroyObject() throws Exception {
        System.out.println("destroyObject");
        session = instance.create(localConnection);
        instance.destroyObject(localConnection, instance.wrap(session));
        assertFalse(session.isConnected());
    }

    @Test
    public void testValidateObjectTrue() throws Exception {
        System.out.println("validateObject/true");
        session = instance.create(localConnection);
        assertTrue(instance.validateObject(localConnection, instance.wrap(session)));
    }
    
    @Test
    public void testValidateObjectFalse() throws Exception {
        System.out.println("validateObject/false");
        session = null;
        assertFalse(instance.validateObject(localConnection, instance.wrap(session)));
    }
}