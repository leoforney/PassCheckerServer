package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.leoforney.passcheckerserver.Main;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
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
            for (String arg: Main.arguments) {
                if (arg.equalsIgnoreCase("idea")) idea = true;
            }
        }

        System.out.println(idea);

        String fileName;
        if (!idea) {
            fileName = "main/version.properties";
        } else {
            fileName = "../resources/version.properties";
        }
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        Properties prop = new Properties();
        try {
            FileReader fileReader = new FileReader(classLoader.getResource(fileName).getFile());
            prop.load(fileReader);
        } catch (IOException e) {
            e.printStackTrace();
        }

        add(new Label("Version: " + prop.getProperty("version")));
        add(new Label("Build Date: " + prop.getProperty("time")));
        add(new Label("Hash: " + prop.getProperty("hash")));
        add(new Label("Java Version: " + System.getProperty("java.version")));
        add(new Label("Vaadin Version: " + prop.get("vaadin")));
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String addr = i.getHostAddress();
                    if (addr.contains("192.168.") && !addr.contains("56.1")) {
                        add(new Label("IP: " + addr));
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        add(new Label("Made by BDSL: Bria R, Dana L., Santiago C., Leo"));
    }

}
