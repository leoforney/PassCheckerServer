package tk.leoforney.passcheckerserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static spark.Spark.port;
import static spark.Spark.staticFiles;
import static tk.leoforney.passcheckerserver.Main.wd;

/**
 * Created by Leo on 4/30/2018.
 */

public class Runner {

    String[] args;
    UserManagement userManagement;
    PassManagement passManagement;
    PhotoManagement photoManagement;

    Runner(String[] args) {
        this.args = args;
    }

    protected static Connection connection;

    boolean run() throws Exception {

        System.out.println("PassChecker Server starting up");

        connection = DriverManager.getConnection("jdbc:sqlite:" + wd + File.separator +"PassCheckerDatabase.db");
        connection.setAutoCommit(false);

        File uploadDir = new File(wd + File.separator + "upload");
        uploadDir.mkdir();
        staticFiles.externalLocation(String.valueOf(uploadDir));

        port(4567);

        userManagement = UserManagement.getInstance();
        passManagement = PassManagement.getInstance();
        photoManagement = new PhotoManagement(passManagement);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Running shutdown hook");
                connection.close();
                photoManagement.alpr.unload();
                if (Main.context != null) {
                    Main.context.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        while (Thread.currentThread().isAlive()) {
            Thread.sleep(350);
        }

        System.out.println("PassChecker shutting down");
        Thread.currentThread().interrupt();
        return true;
    }

}
