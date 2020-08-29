package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.sshd.client.session.ClientSession;

/**
 * Manage ssh connections and share existing connections
 */
public class SSHConnectionPool extends GenericKeyedObjectPool<ConnectionDetails, ClientSession> {
    public SSHConnectionPool(SSHSessionFactory factory, GenericKeyedObjectPoolConfig<ClientSession> config) {
        super(factory, config);
    }

    public static SSHConnectionPool createInstance() {
        GenericKeyedObjectPoolConfig<ClientSession> config = new GenericKeyedObjectPoolConfig<>();
        config.setMinIdlePerKey(0);
        config.setTestOnBorrow(true);
        return new SSHConnectionPool(new SSHSessionFactory(), config);
    }
}
