package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.io.ByteArrayOutputStream;
import java.text.NumberFormat;
import java.util.EnumSet;
import java.util.Locale;
import kg.apc.jmeter.vizualizers.MonitoringSampler;
import kg.apc.jmeter.vizualizers.MonitoringSampleGenerator;
import org.apache.commons.lang3.LocaleUtils;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.sshd.client.channel.ChannelExec;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;

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
    private NumberFormat numberFormat = NumberFormat.getInstance(LocaleUtils.toLocale(JMeterUtils.getPropDefault("jmeter.sshmon.locale", Locale.getDefault().toString())));

    /**
     * Manage ssh connections and share existing connections
     */
    private static SSHConnectionPool pool;

    public static void init() {
        log.debug("Opening connection pool");
        pool = SSHConnectionPool.createInstance();
        SecurityProviderLoader.addJceProvider(JMeterUtils.getPropDefault("jmeter.sshmon.jceprovider", "org.bouncycastle.jce.provider.BouncyCastleProvider"));
    }

    public static void closeConnectionPool() {
        log.debug("Closing connection pool");
        try {
            pool.close();
        }
        catch (Exception e) {
            log.error("Failed to close connection pool: ", e);
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
        ClientSession session = null; // https://github.com/apache/mina-sshd/blob/master/docs/client-setup.md#keeping-the-session-alive-while-no-traffic
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        try {
            log.debug("Borrowing session for "+connectionDetails);
            session = pool.borrowObject(connectionDetails);

            try (ChannelExec channel = session.createExecChannel(remoteCommand, null,null)) { // TODO
                channel.setUsePty(true);
                channel.setOut(result);
                channel.open();
                channel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 1000L);  // wait for command execution to finish
            }

            final double val = numberFormat.parse(result.toString()).doubleValue();
            if (sampleDeltaValue) {
                if (!Double.isNaN(oldValue)) {
                    collector.generateSample(val - oldValue, metricName);
                }
                oldValue = val;
            } else {
                collector.generateSample(val, metricName);
            }
        }
        catch (Exception ex) {
            log.error("Sample failure for "+connectionDetails, ex);
        }
        finally {
            try {
                if (session != null) {
                    if (session.isOpen()) {
                        log.debug("Returning session for " + connectionDetails);
                        pool.returnObject(connectionDetails, session);
                    } else {
                        log.debug("Invalidating session for " + connectionDetails);
                        pool.invalidateObject(connectionDetails, session);
                    }
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
