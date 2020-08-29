package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import kg.apc.emulators.TestJMeterUtils;
import kg.apc.jmeter.vizualizers.MonitoringSampleGenerator;
import java.util.Locale;
import org.apache.jmeter.util.JMeterUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test requires an SSH server to be started externally 
 * (currently via process-exec-maven-plugin)
 */
public class SSHMonSamplerIT {
    public static ConnectionDetails localConnection = SSHSessionFactoryIT.localConnection;

    public class MockSampleGenerator implements MonitoringSampleGenerator {
        protected String metric = "";
        protected double value = Double.NaN;
        protected boolean collected = false;
        
        @Override
        public void generateSample(double d, String string) {
            this.metric = string;
            this.value = d;
            this.collected = true;
        }
    }

    MockSampleGenerator collector = new MockSampleGenerator();

    @BeforeClass
    public static void setUpClass() {
        TestJMeterUtils.createJmeterEnv();
    }

    @Test
    public void testGenerateSamples() {
        SSHMonSampler instance = new SSHMonSampler("test", localConnection, "echo 123", false);
        instance.generateSamples(collector);
        assertTrue(collector.collected);
        assertEquals(123.0, collector.value,  0.0);
        assertEquals("test", collector.metric);
    }

    @Test
    public void testSamplesWithDefaultLocale() {
        Locale restore = Locale.getDefault();
        Locale.setDefault(Locale.GERMAN);
        SSHMonSampler instance = new SSHMonSampler("test", localConnection, "echo 1.234,5", false);
        instance.generateSamples(collector);
        assertEquals(1234.5, collector.value, 0.0);
        Locale.setDefault(restore);
    }

    @Test
    public void testSamplesWithExplicitLocale() {
        JMeterUtils.setProperty("jmeter.sshmon.locale", "de_DE");
        SSHMonSampler instance = new SSHMonSampler("test", localConnection, "echo 1.234,5", false);
        instance.generateSamples(collector);
        assertEquals(1234.5, collector.value, 0.0);
        JMeterUtils.setProperty("jmeter.sshmon.locale", "");
    }
}
