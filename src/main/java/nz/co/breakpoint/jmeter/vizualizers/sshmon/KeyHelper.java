package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;
import org.apache.sshd.common.NamedResource;
import org.apache.sshd.common.config.keys.FilePasswordProvider;
import org.apache.sshd.common.config.keys.loader.KeyPairResourceParser;
import org.apache.sshd.common.util.security.SecurityUtils;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.KeyPair;

import java.util.Arrays;
import java.util.List;

public class KeyHelper {

    private static final Logger log = LoggingManager.getLoggerForClass();

    public static KeyPair toKeyPair(String privateKey, String password) throws IOException {
        final NamedResource dummy = NamedResource.ofName("");
        final List<String> lines = Arrays.asList(privateKey.split("\n"));
        log.debug("Extracting key pair from ["+lines+"]");
        KeyPairResourceParser parser = SecurityUtils.getKeyPairResourceParser();
        try {
            if (parser.canExtractKeyPairs(dummy, lines)) {
                return parser.loadKeyPairs(null, dummy, FilePasswordProvider.of(password), lines).iterator().next();
            }
            log.error("Unsupported private key format '"+privateKey+"'");
        } catch (GeneralSecurityException e) {
            log.error("Failed to process private key '"+privateKey+"'", e);
        }
        return null;
    }
}
