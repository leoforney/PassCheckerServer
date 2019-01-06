package tk.leoforney.passcheckerserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.File;

/**
 * Created by Leo on 4/30/2018.
 */
@SpringBootApplication
@ServletComponentScan
@EntityScan
public class Main {

    public static String wd = System.getProperty("user.home") + File.separator + "Desktop";
    public static ConfigurableApplicationContext context;
    public static String[] arguments;

    public static void main(String[] args) {
        Runner runner = new Runner(args);
        context = SpringApplication.run(Main.class, args);
        arguments = args;
        try {
            runner.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Exited Runner");
        System.exit(1);
    }

}
