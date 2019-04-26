package tk.leoforney.passcheckerserver;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by Leo on 4/30/2018.
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@ServletComponentScan
@EntityScan
public class Main extends Application {

    public static String wd = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "PassCheckerServer";
    public static ConfigurableApplicationContext context;
    public static String[] arguments;

    public static void main(String[] args) {
        context = SpringApplication.run(Main.class, args);
        Runner runner = new Runner(args);
        new Thread(() -> {
            arguments = args;
            try {
                runner.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Exited Runner");
            System.exit(1);
        }).start();
        launch(args);

    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.of(5, DataUnit.MEGABYTES));
        factory.setMaxRequestSize(DataSize.of(5, DataUnit.MEGABYTES));
        return factory.createMultipartConfig();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setOnCloseRequest(event -> {
            System.out.println("Stage is closing");
            Runner.on = false;
        });

        primaryStage.setTitle("PassCheckerServer");

        InputStream stream = Main.class.getResourceAsStream("../../../launcher.png");
        if (stream != null) {
            primaryStage.getIcons().add(new Image(stream));
        }

        TextArea ta = new TextArea();
        ta.setEditable(false);

        ScanLogger.getInstance().addListener(string -> ta.appendText(System.currentTimeMillis() + " - " + string.toUpperCase() + "\n"));

        BorderPane pane = new BorderPane();
        pane.setCenter(ta);

        ScanLogger.getInstance().log("In directory: " + wd);

        Scene app = new Scene(pane, 800, 600);
        primaryStage.setScene(app);
        primaryStage.show();
    }
}
