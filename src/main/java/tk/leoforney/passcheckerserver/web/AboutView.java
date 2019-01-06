package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.leoforney.passcheckerserver.Main;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;

import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route("about")
public class AboutView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(AboutView.class);

    public AboutView() {
        H2 title = new H2("About");
        add(title);
        setTitle(title);

        boolean idea = false;
        if (Main.arguments.length > 0) {
            idea = Main.arguments[0].equalsIgnoreCase("idea");
        }

        System.out.println(idea);

        String fileName;
        if (!idea) {
            fileName = "main/version.properties";
        } else {
            fileName = "../resources/version.properties";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        File file = new File("");
        System.out.println(file.getAbsolutePath());
        System.out.println(classLoader.getResource(fileName).getPath());

        Properties prop = new Properties();
        try {
            FileReader fileReader = new FileReader(classLoader.getResource(fileName).getFile());
            prop.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(new Label("Version: " + prop.getProperty("version")));
        add(new Label("Time: " + prop.getProperty("time")));
        add(new Label("Vaadin Version: " + prop.get("vaadin")));
        add(new Label("Made by BDSL: Bria R, Dana L., Santiago C., Leo"));
    }

}
