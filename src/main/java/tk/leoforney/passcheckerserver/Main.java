package tk.leoforney.passcheckerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.unit.DataSize;
import org.springframework.util.unit.DataUnit;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * Created by Leo on 4/30/2018.
 */
@SpringBootApplication(exclude = {MongoAutoConfiguration.class})
@ServletComponentScan
@EntityScan
public class Main extends SpringBootServletInitializer {

    public static String wd = System.getProperty("user.home") + File.separator + "Desktop" + File.separator + "PassCheckerServer";
    public static ConfigurableApplicationContext context;
    public static String[] arguments;

    public static void main(String[] args) {
        Runner runner = new Runner(args);
        context = SpringApplication.run(Main.class, args);
        context.getAutowireCapableBeanFactory().initializeBean(Runner.connection, "sqliteConnectionDatabase");
        arguments = args;
        try {
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exited Runner");
        System.exit(1);
    }

    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        factory.setMaxFileSize(DataSize.of(5, DataUnit.MEGABYTES));
        factory.setMaxRequestSize(DataSize.of(5, DataUnit.MEGABYTES));
        return factory.createMultipartConfig();
    }

}
