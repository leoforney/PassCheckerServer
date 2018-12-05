package tk.leoforney.passcheckerserver;

import java.io.File;

/**
 * Created by Leo on 4/30/2018.
 */
public class Main {

    public static String wd = System.getProperty("user.home") + File.separator + "Desktop";

    public static void main(String[] args) {
        new Thread(() -> PhotoViewer.main(args)).start();
        Runner runner = new Runner(args);
        try {
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exited Runner");
        System.exit(1);
    }

}
