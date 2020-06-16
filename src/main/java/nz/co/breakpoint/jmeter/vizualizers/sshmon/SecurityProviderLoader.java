package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.Provider;
import java.security.Security;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Logger;

// Source: apache/ws-wss4j/ws-security-common/src/main/java/org/apache/wss4j/common/crypto/WSProviderConfig.java
public class SecurityProviderLoader {
    private static final Logger log = LoggingManager.getLoggerForClass();

    static void addJceProvider(String className) {
		AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
            public Boolean run() {
                try {
                    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    //              ClassLoader loader = ClassLoader.getSystemClassLoader();
                    Class<? extends Provider> clazz = loader.loadClass(className).asSubclass(Provider.class);
                    Provider provider = clazz.newInstance();
                    String name = provider.getName();
                    if (Security.getProvider(name) != null) {
                        log.debug("JCE Provider " + name + " already registered");
                    } else {
                        int n = Security.addProvider(provider);
                        log.debug("JCE provider " + name + " - " + provider.getVersion() + " was added at position: " + n);
                    }
                } catch (Throwable t) {
                    log.error("JCE provider " + className + " failed to register ", t);
                }
                return true;
            }
        });
    }
}
