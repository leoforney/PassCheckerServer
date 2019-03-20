package tk.leoforney.passcheckerserver;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.vaadin.flow.component.notification.Notification;
import spark.servlet.SparkApplication;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import static spark.Spark.get;
import static spark.Spark.staticFiles;
import static tk.leoforney.passcheckerserver.Main.wd;

/**
 * Created by Leo on 4/30/2018.
 */

public class Runner implements SparkApplication {

    String[] args;
    UserManagement userManagement;
    PassManagement passManagement;
    PhotoManagement photoManagement;
    protected static Properties properties;

    public Runner(String[] args) {
        this.args = args;
    }

    public Runner() {
        this.args = null;
    }

    protected static Connection connection;
    protected static MongoClient mongoClient;
    protected static MongoDatabase checkDatabase;

    boolean run() throws Exception {
        while (Thread.currentThread().isAlive()) {
            Thread.sleep(350);
        }

        System.out.println("PassChecker shutting down");
        Thread.currentThread().interrupt();
        return true;
    }

    public static final String[] PATHS = {"/getProperty/*"};

    void registerHooks() {

        get("/getProperty/*", (request, response) -> {
            String propertyKey = request.splat()[0];
            if (propertyKey.contains("mongo")) {
                return "Not permitted";
            }
            return properties.getProperty(propertyKey, "Property not set");
        });
    }

    private void initializePreRequisites() throws Exception {
        System.out.println("PassChecker Server starting up");

        connection = DriverManager.getConnection("jdbc:sqlite:" + wd + File.separator + "PassCheckerDatabase.db");
        connection.setAutoCommit(false);

        properties = new Properties();
        InputStreamReader in = null;
        try {
            in = new InputStreamReader(new FileInputStream(wd + File.separator + "PassCheckerServer.properties"), "UTF-8");
            properties.load(in);
        } finally {
            if (null != in) {
                try {
                    in.close();
                } catch (IOException ex) {
                }
            }
        }

        String mongoHost = properties.getProperty("mongoHost", "localhost");
        int mongoPort = Integer.valueOf(properties.getProperty("mongoPort", "27017"));
        ServerAddress mongoAddress = new ServerAddress(mongoHost, mongoPort);

        String mongoUserName = properties.getProperty("mongoUser", null);
        String mongoPassword = properties.getProperty("mongoPassword", null);
        String mongoDb = properties.getProperty("mongoDb", "passcheck_log");
        if (mongoUserName == null || mongoPassword == null) {
            mongoClient = MongoClients.create("mongodb://" + mongoHost + ":" + mongoPort);
        } else {
            mongoClient = MongoClients.create("mongodb://" + mongoUserName + ":" + mongoPassword + "@"
                    + mongoHost + ":" + mongoPort + "/" + mongoDb);
        }
        checkDatabase = mongoClient.getDatabase("passcheck_log");

        userManagement = UserManagement.getInstance();
        passManagement = PassManagement.getInstance();
        passManagement.getStudentList();
        photoManagement = new PhotoManagement(passManagement);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                System.out.println("Running shutdown hook");
                connection.close();
                mongoClient.close();
                photoManagement.alpr.unload();
                if (Main.context != null) {
                    Main.context.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public static String UPLOADDIR = new File(wd + File.separator + "upload").getAbsolutePath();

    @Override
    public void init() {

        try {
            initializePreRequisites();
        } catch (Exception e) {
            e.printStackTrace();
        }

        registerHooks();
        userManagement.registerHooks();
        File uploadDir = new File(wd + File.separator + "upload");
        uploadDir.mkdir();
        staticFiles.externalLocation(String.valueOf(uploadDir));
    }

    @Override
    public void destroy() {

    }

    public static void show(String message) {
        Notification.show(message, 1000, Notification.Position.BOTTOM_END);
    }
}
