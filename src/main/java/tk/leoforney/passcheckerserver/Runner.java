package tk.leoforney.passcheckerserver;

import spark.Request;

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

    void run() throws Exception {

        System.out.println("PassChecker Server starting up");

        connection = DriverManager.getConnection("jdbc:sqlite:" + wd + File.separator +"PassCheckerDatabase.db");
        connection.setAutoCommit(false);

        File uploadDir = new File(wd + File.separator + "upload");
        uploadDir.mkdir();
        staticFiles.externalLocation(String.valueOf(uploadDir));

        port(4567);

        userManagement = new UserManagement(connection);
        passManagement = new PassManagement(connection);
        photoManagement = new PhotoManagement(passManagement);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                connection.close();
                photoManagement.alpr.unload();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));

        while (Thread.currentThread().isAlive()) {
            Thread.sleep(350);
        }
    }

}
