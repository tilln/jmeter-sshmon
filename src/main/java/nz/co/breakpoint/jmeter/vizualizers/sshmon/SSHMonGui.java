package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import kg.apc.jmeter.graphs.AbstractMonitoringVisualizer;
import kg.apc.jmeter.vizualizers.MonitoringResultsCollector;

/**
 * GUI class defining the config settings of monitoring samplers
 * that collect samples from commands executed remotely via SSH.
 * Uses an SSHMonCollector object to manage a list of SSHMonSamplers.
 */
public class SSHMonGui
        extends AbstractMonitoringVisualizer {

    protected static String[] columnIdentifiers = new String[]{
        "Label", "Host", "Port", "Username", "Private Key (PEM)", "Password", "Command", "Delta"
    };
    protected static Class[] columnClasses = new Class[]{
        String.class, String.class, String.class, String.class, String.class, String.class, String.class, Boolean.class
    };
    protected static Object[] defaultValues = new Object[]{
        "", "", "22", "", "", "", "", false
    };
    protected static int[] columnWidths = new int[]{
        100, 100, 50, 100, 120, 100, 500, 50
    };

    public static final String WIKI_PAGE = "https://github.com/tilln/jmeter-sshmon";

    @Override
    protected String[] getColumnIdentifiers() { return columnIdentifiers; }
    @Override
    protected Class[] getColumnClasses() { return columnClasses; }
    @Override
    protected Object[] getDefaultValues() { return defaultValues; }
    @Override
    protected int[] getColumnWidths() { return columnWidths; }

    @Override
    public String getWikiPage() {
        return WIKI_PAGE;
    }

    @Override
    public String getStaticLabel() {
        return "SSHMon Samples Collector";
    }

    @Override
    protected MonitoringResultsCollector createMonitoringResultsCollector() {
        return new SSHMonCollector();
    }
}