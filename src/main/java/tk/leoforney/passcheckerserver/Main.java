package tk.leoforney.passcheckerserver;

import java.io.File;

/**
 * Created by Leo on 4/30/2018.
 */
public class Main {

    public static String wd = System.getProperty("user.home") + File.separator + "Desktop";

    public static void main(String[] args) {
        if (args.length > 0) {
            if (args[0].equals("gui")) {
                PhotoViewer.launch(args);
            }
        }
        Runner runner = new Runner(args);
        try {
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
