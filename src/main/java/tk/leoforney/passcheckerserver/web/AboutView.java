package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

public class AboutView extends VerticalLayout {

    public AboutView() {
        H2 title = new H2("About");
        add(title);
        setTitle(title);
        FileInputStream input = null;
        try {
            input = new FileInputStream("main/version.properties");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Properties prop = new Properties();

        // load a properties file
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            prop.load(classLoader.getClass().getClassLoader().getResourceAsStream("main/version.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        add(new Label("Version: " + prop.getProperty("version")));
    }

}
