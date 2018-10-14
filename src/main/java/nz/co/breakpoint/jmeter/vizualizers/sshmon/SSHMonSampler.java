package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.io.ByteArrayOutputStream;

import kg.apc.jmeter.vizualizers.MonitoringSampler;
import kg.apc.jmeter.vizualizers.MonitoringSampleGenerator;

import org.apache.commons.pool2.KeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPool;
import org.apache.commons.pool2.impl.GenericKeyedObjectPoolConfig;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Monitoring sampler that collects the numeric output of a remote command 
 * executed over a pooled SSH session.
 */
public class SSHMonSampler
        implements MonitoringSampler {

    private static final Logger log = LoggingManager.getLoggerForClass();
    private String metricName;
    private ConnectionDetails connectionDetails;
    private String remoteCommand;
    private boolean sampleDeltaValue = true;
    private double oldValue = Double.NaN;

    /**
     * Manage ssh connections and share existing connections
     */
    private static KeyedObjectPool<ConnectionDetails, Session> pool;
    static {
        GenericKeyedObjectPoolConfig<Session> config = new GenericKeyedObjectPoolConfig<>();
        config.setMinIdlePerKey(1);
        config.setTestOnBorrow(true);
        log.debug("Creating GenericKeyedObjectPool");
        pool = new GenericKeyedObjectPool<ConnectionDetails, Session>(new SSHSessionFactory(), config);
    }

    public static void clearConnectionPool() {
        log.debug("Clearing connection pool");
        try {
            pool.clear();
        }
        catch (Exception e) {
            log.error("Failed to clear connection pool: ", e);
        }
    }

    public SSHMonSampler(String name, ConnectionDetails connectionDetails, String remoteCommand, boolean sampleDeltaValue) {
        this.metricName = name;
        this.connectionDetails = connectionDetails;
        this.remoteCommand = remoteCommand;
        this.sampleDeltaValue = sampleDeltaValue;
    }

    @Override
    public void generateSamples(MonitoringSampleGenerator collector) {
        Session session = null;
        ChannelExec channel = null;
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try {
            log.debug("Borrowing session for "+connectionDetails);
            session = pool.borrowObject(connectionDetails);

            channel = (ChannelExec)session.openChannel("exec");
            channel.setCommand(remoteCommand);
            channel.setPty(true);
            channel.setOutputStream(result);
            channel.connect();

            while (!channel.isClosed()) { // wait for command execution to finish
                Thread.sleep(10);
            }

            final double val = Double.valueOf(result.toString());
            if (sampleDeltaValue) {
                if (!Double.isNaN(oldValue)) {
                    collector.generateSample(val - oldValue, metricName);
                }
                oldValue = val;
            } else {
                collector.generateSample(val, metricName);
            }
        }
        catch (JSchException ex) {
            log.error("Channel failure for "+connectionDetails, ex);
        }
        catch (Exception ex) {
            log.error("Sample failure for "+connectionDetails, ex);
        }
        finally {
            if (channel != null) {
                log.debug("Disconnecting channel for "+connectionDetails);
                channel.disconnect();
            }
            try {
                if (session != null && session.isConnected()) {
                    log.debug("Returning session for "+connectionDetails);
                    pool.returnObject(connectionDetails, session);
                }
                else {
                    log.debug("Invalidating session for "+connectionDetails);
                    pool.invalidateObject(connectionDetails, session);
                }
            }
            catch (Exception ex) {
                log.warn("Failure returning session ", ex);
            }
        }
    }

    public String getMetricName() {
        return metricName;
    }

    public void setMetricName(String metricName) {
        this.metricName = metricName;
    }

    public ConnectionDetails getConnectionDetails() {
        return connectionDetails;
    }
    
    public void setConnectionDetails(ConnectionDetails connectionDetails) {
        this.connectionDetails = connectionDetails;
    }
    
    public String getRemoteCommand() {
        return remoteCommand;
    }
    
    public void setRemoteCommand(String remoteCommand) {
        this.remoteCommand = remoteCommand;
    }
    
    public boolean isSampleDeltaValue() {
        return sampleDeltaValue;
    }

    public void setSampleDeltaValue(boolean sampleDeltaValue) {
        this.sampleDeltaValue = sampleDeltaValue;
    }

    public double getOldValue() {
        return oldValue;
    }

    public void setOldValue(double oldValue) {
        this.oldValue = oldValue;
    }
}
