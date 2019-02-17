package tk.leoforney.passcheckerserver;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static spark.Spark.*;
import static tk.leoforney.passcheckerserver.Main.wd;

/**
 * Created by Leo on 4/30/2018.
 */

public class Runner {

    String[] args;
    UserManagement userManagement;
    PassManagement passManagement;
    PhotoManagement photoManagement;
    protected static Properties properties;

    Runner(String[] args) {
        this.args = args;
    }

    protected static Connection connection;
    protected static MongoClient mongoClient;
    protected static MongoDatabase checkDatabase;

    boolean run() throws Exception {

        System.out.println("PassChecker Server starting up");

        connection = DriverManager.getConnection("jdbc:sqlite:" + wd + File.separator + "PassCheckerDatabase.db");
        connection.setAutoCommit(false);

        mongoClient = MongoClients.create();
        checkDatabase = mongoClient.getDatabase("passcheck_log");

        File uploadDir = new File(wd + File.separator + "upload");
        uploadDir.mkdir();
        staticFiles.externalLocation(String.valueOf(uploadDir));

        port(4567);

        userManagement = UserManagement.getInstance();
        passManagement = PassManagement.getInstance();
        photoManagement = new PhotoManagement(passManagement);

        properties = new Properties();
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(wd + File.separator + "PassCheckerServer.properties"), "UTF-8");
            properties.load(in);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ex) {}
            }
        }

        get("/getProperty/*", (request, response) -> {
            String propertyKey = request.splat()[0];
            return properties.getProperty(propertyKey);
        });

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
