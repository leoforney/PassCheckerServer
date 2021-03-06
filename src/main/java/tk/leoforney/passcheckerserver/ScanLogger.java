package tk.leoforney.passcheckerserver;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static tk.leoforney.passcheckerserver.Main.wd;

public class ScanLogger {

    private static ScanLogger instance = null;

    public static ScanLogger getInstance() {
        if (instance == null) {
            instance = new ScanLogger();
        }
        return instance;
    }

    List<LogListener> listeners;

    private File logFile;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean enabled = true;

    public void setLogFile(String fileName) {
        logFile = new File(wd + File.separator + fileName);
        /*
        if (logFile.exists()) {
            //logFile.delete();
        }
        try {
            //logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ScanLogger() {
        setLogFile("scan.log");
        log("PassCheckerServer log starting");
        listeners = new ArrayList<>();

    }

    public void addListener(LogListener listener) {
        if (listener != null) {
            System.out.println("Listener added!");
            listeners.add(listener);
            listener.logWritten("PassCheckerServer successfully started");
        }
    }

    public void removeListener(LogListener listener) {
        listeners.remove(listener);
    }

    public void log(String data) {
        if (listeners != null && listeners.size() > 0) {
            for (LogListener listener : listeners) {
                if (listener == null) {
                    listeners.remove(listener);
                }
            }
        }
        if (logFile != null && enabled) {
            try {
                FileUtils.writeStringToFile(logFile, System.currentTimeMillis() + " - " + data.toUpperCase() + "\n", true);
                if (listeners != null && listeners.size() > 0) {
                    for (LogListener listener : listeners) {
                        listener.logWritten(data);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
