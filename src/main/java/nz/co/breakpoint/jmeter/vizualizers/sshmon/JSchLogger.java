package nz.co.breakpoint.jmeter.vizualizers.sshmon;

import java.util.HashMap;
import java.util.Map;
import com.jcraft.jsch.Logger;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.log.Priority;

class JSchLogger implements Logger {

    private static final org.apache.log.Logger log = LoggingManager.getLoggerForClass();

    private static final Map<Integer, Priority> levels = new HashMap<Integer, Priority>();
    static {
        levels.put(Logger.DEBUG, Priority.DEBUG);
        levels.put(Logger.INFO, Priority.INFO);
        levels.put(Logger.WARN, Priority.WARN);
        levels.put(Logger.ERROR, Priority.ERROR);
        levels.put(Logger.FATAL, Priority.FATAL_ERROR);
    }

    @Override
    public boolean isEnabled(int level) {
        return log.isPriorityEnabled(levels.get(level));
    }

    @Override
    public void log(int level, String message) {
        Priority p = levels.get(level);
        if (p != null) {
            log.log(p, message);
        }
    }
}