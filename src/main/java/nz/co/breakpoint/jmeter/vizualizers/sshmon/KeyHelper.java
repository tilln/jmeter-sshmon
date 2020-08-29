package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.pem.PEMResourceParserUtils;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import java.util.Arrays;
import java.util.List;

// TODO unit tests
public class KeyHelper {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static KeyPair toKeyPair(String privateKey, String password) throws IOException {
        final NamedResource dummy = NamedResource.ofName("");
        final List<String> lines = Arrays.asList(privateKey.split("\n"));
        log.debug("Extracting key pair from ["+lines+"]");
        try {
            if (PEMResourceParserUtils.PROXY.canExtractKeyPairs(dummy, lines)) {
                return PEMResourceParserUtils.PROXY.loadKeyPairs(null, dummy, FilePasswordProvider.of(password), lines).iterator().next();
            }
            log.error("Unsupported private key format '"+privateKey+"'");
        } catch (GeneralSecurityException e) {
            log.error("Failed to process private key '"+privateKey+"'", e);
        }
        return null;
    }
}
