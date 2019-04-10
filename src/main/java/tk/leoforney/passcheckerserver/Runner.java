package tk.leoforney.passcheckerserver;

import com.mongodb.ServerAddress;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.vaadin.flow.component.notification.Notification;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.Main.wd;

/**
 * Created by Leo on 4/30/2018.
 */

@RestController
public class Runner {

    String[] args;
    UserManagement userManagement;
    PassManagement passManagement;
    PhotoManagement photoManagement;
    protected static Properties properties;
    private final static Logger logger = Logger.getLogger(Runner.class.getName());

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

        try {
            initializePreRequisites();
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (Thread.currentThread().isAlive()) {
            Thread.sleep(350);
        }

        logger.log(Level.INFO, "PassChecker shutting down");
        Thread.currentThread().interrupt();
        return true;
    }

    public static final String[] PATHS = {"/getProperty/*"};

    @RequestMapping(value = {"/getProperty/**"},  params="propertyKey", method = RequestMethod.GET)
    public String getProperty(@RequestParam(value = "propertyKey", required=false) String propertyKey) {
        if (propertyKey != null && propertyKey.contains("mongo")) {
            return "Not permitted";
        }
        return properties.getProperty(propertyKey, "Property not set");
    }


    private void initializePreRequisites() throws Exception {
        logger.log(Level.INFO, "PassChecker Server starting up");



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
                connection.close();
                mongoClient.close();
                photoManagement.alpr.unload();
                PhotoQueue.getInstance().getAlpr().unload();
                if (Main.context != null) {
                    Main.context.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }));
    }

    public static void show(String message) {
        Notification.show(message, 1000, Notification.Position.BOTTOM_END);
    }
}
