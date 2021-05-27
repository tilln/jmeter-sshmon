package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertNotNull;

public class KeyHelperTest {

    String getFileContent(String filename) throws IOException {
        return new String(Files.readAllBytes(Paths.get(filename)));
    }

    @Test
    public void testRSAFormat() throws IOException {
        assertNotNull(KeyHelper.toKeyPair(getFileContent("src/test/resources/hostkey.key"), ""));
    }

    @Test
    public void testOpenSSHFormat() throws IOException {
        assertNotNull(KeyHelper.toKeyPair(getFileContent("src/test/resources/openssh.key"), "changeit"));
    }
}
