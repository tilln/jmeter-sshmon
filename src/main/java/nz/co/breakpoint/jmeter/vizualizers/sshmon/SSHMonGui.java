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
        String.class, String.class, Integer.class, String.class, String.class, String.class, String.class, Boolean.class
    };
    protected static Object[] defaultValues = new Object[]{
        "", "", 22, "", "", "", "", false
    };
    protected static int[] columnWidths = new int[]{
        100, 100, 50, 100, 100, 100, 500, 50
    };

    public static final String WIKI_PAGE = "https://github.com/tilln/jmeter-sshmon";

    // WORKAROUND to replace the help link added by JMeterPluginsUtils.addHelpLinkToPanel
    public SSHMonGui() {
        org.apache.jmeter.gui.GuiPackage guiPack = org.apache.jmeter.gui.GuiPackage.getInstance();
        if (guiPack != null) {
            org.apache.jmeter.gui.JMeterGUIComponent gui = guiPack.getCurrentGui();
            if (gui instanceof SSHMonGui) {
                replaceHelpLinkPanel((SSHMonGui)gui);
            }
        }
    }

    private static void replaceHelpLinkPanel(javax.swing.JComponent outer) {
        for (int n = 0; n < outer.getComponentCount(); n++) {
            if (outer.getComponent(n) instanceof javax.swing.JComponent) {
                javax.swing.JComponent comp = (javax.swing.JComponent) outer.getComponent(n);
                if (comp instanceof javax.swing.JPanel && ((java.awt.Container)comp).getComponentCount() == 3) {
                    java.awt.Component link = ((java.awt.Container)comp).getComponent(1);
                    if (link instanceof javax.swing.JLabel && ((javax.swing.JLabel)link).getText().equals("Help on this plugin")) {
                        link.removeMouseListener(link.getMouseListeners()[0]);
                        link.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mouseClicked(java.awt.event.MouseEvent e) { kg.apc.jmeter.JMeterPluginsUtils.openInBrowser(WIKI_PAGE); }
                            @Override
                            public void mousePressed(java.awt.event.MouseEvent e) {}
                            @Override
                            public void mouseReleased(java.awt.event.MouseEvent e) {}
                            @Override
                            public void mouseEntered(java.awt.event.MouseEvent e) {}
                            @Override
                            public void mouseExited(java.awt.event.MouseEvent e) {}
                        });
                        return;
                    }
                }
                replaceHelpLinkPanel(comp);
            }
        }
    }
    // /WORKAROUND END

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