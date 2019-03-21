package tk.leoforney.passcheckerserver;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import static tk.leoforney.passcheckerserver.Main.wd;

public class ScanLogger {

    private static ScanLogger instance = null;

    public static ScanLogger getInstance() {
        if (instance == null) {
            instance = new ScanLogger();
        }
        return instance;
    }

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
        if (logFile.exists()) {
            logFile.delete();
        }
        try {
            logFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private ScanLogger() {
        setLogFile("scan.log");
        log("PassCheckerServer log starting");
    }

    public void log(String data) {
        if (logFile != null && enabled) {
            try {
                FileUtils.writeStringToFile(logFile, System.currentTimeMillis() + " - " + data.toUpperCase() + "\n", true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
