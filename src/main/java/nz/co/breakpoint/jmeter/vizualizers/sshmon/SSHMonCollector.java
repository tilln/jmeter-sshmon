package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.util.ArrayList;
import kg.apc.jmeter.vizualizers.MonitoringResultsCollector;
import org.apache.jmeter.samplers.SampleEvent;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.testelement.property.CollectionProperty;
import org.apache.jmeter.testelement.property.JMeterProperty;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

/**
 * Class that collects SSHMonSampler sample results.
 * Acts as a link between the SSHMonGui and the actual samplers.
 * Implementation of the abstract MonitoringResultsCollector.
 */
public class SSHMonCollector
        extends MonitoringResultsCollector {

    private static final long serialVersionUID = 1L;
    private static final Logger log = LoggingManager.getLoggerForClass();

    @Override
    protected String getPrefix() { return "SSHMon"; }

    @Override
    protected String getForceFilePropertyName() { return "jmeter.sshmon.forceOutputFile"; }

    @Override
    protected int getInterval() {
        return JMeterUtils.getPropDefault("jmeter.sshmon.interval", 1000);
    }

    @Override
    protected void initiateConnectors() {
        samplers.clear();
        CollectionProperty rows = getSamplerSettings();

        for (int i = 0; i < rows.size(); i++) {
            ArrayList<Object> row = (ArrayList<Object>) rows.get(i).getObjectValue();
            String  label      = ((JMeterProperty)row.get(0)).getStringValue();
            String  host       = ((JMeterProperty)row.get(1)).getStringValue();
            int     port       = ((JMeterProperty)row.get(2)).getIntValue();
            String  username   = ((JMeterProperty)row.get(3)).getStringValue();
            String  privateKey = ((JMeterProperty)row.get(4)).getStringValue();
            String  password   = ((JMeterProperty)row.get(5)).getStringValue();
            String  command    = ((JMeterProperty)row.get(6)).getStringValue();
            boolean isDelta    = ((JMeterProperty)row.get(7)).getBooleanValue();

            ConnectionDetails connectionDetails = new ConnectionDetails(username, host, port, password, privateKey);

            log.debug("Adding sampler for "+connectionDetails+" / "+command);
            samplers.add(new SSHMonSampler(label, connectionDetails, command, isDelta));
        }
    }

    @Override
    public void testStarted(String host) {
        super.testStarted(host);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> SSHMonSampler.closeConnectionPool()));
    }

    @Override
    protected void monitoringSampleOccurred(SampleEvent event) {
        SampleResult res = event.getResult();
        // Fix #5: end time stamp not set
        // https://github.com/undera/jmeter-plugins/blob/c8bf66d3f6742d7391764890cf110faae597b4fd/infra/common/src/main/java/kg/apc/jmeter/vizualizers/MonitoringSampleResult.java#L16
        if (res != null && res.getEndTime() == 0) { res.setEndTime(res.getStartTime()); }
        super.monitoringSampleOccurred(event);
    }
}

