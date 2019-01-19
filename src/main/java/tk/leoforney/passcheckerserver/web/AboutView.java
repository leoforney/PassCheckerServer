package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.dependency.JavaScript;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Spark;
import tk.leoforney.passcheckerserver.Main;

import java.io.FileReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static tk.leoforney.passcheckerserver.UserManagement.authenticated;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route("about")
@JavaScript("bower_components/jquery/dist/jquery.min.js")
@JavaScript("bower_components/jquery-qrcode/jquery.qrcode.min.js")
public class AboutView extends VerticalLayout {

    private static Logger logger = LoggerFactory.getLogger(AboutView.class);
    private Base64 base64;

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
        base64 = new Base64();

        add(new Label("Version: " + prop.getProperty("version")));
        add(new Label("Build Date: " + prop.getProperty("time")));
        add(new Label("Hash: " + prop.getProperty("hash")));
        add(new Label("Java Version: " + System.getProperty("java.version")));
        add(new Label("Vaadin Version: " + prop.get("vaadin")));
        List<String> ips = ipAddress();
        for (String string: ips) {
            add(new Label("IP: " + string));
        }
        add(new Label("Made by BDSL: Bria R, Dana L., Santiago C., Leo"));
        Object tokenObject = VaadinSession.getCurrent().getAttribute("Token");

        //QRCode code = null;
        if (tokenObject != null && authenticated(String.valueOf(tokenObject))) {
            String[] hash = Arrays.toString(base64.decode(String.valueOf(tokenObject))).split(":");
            //String email = hash[0];
            //String password = hash[1];
            //String ip = ips.get(0);
            //code = new QRCode(Base64.encodeBase64String((email + ":" + password + ":" + ip).getBytes()));
            Html html = new Html("<div id=\"qrcode\"></div>");
            add(html);
            final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
            executor.schedule(() -> {
                if (getUI().isPresent()) {
                    System.out.println("UI Present");
                    // TODO: Finish QR code
                    getUI().get().getPage().executeJavaScript(
                            "jQuery('#qrcode').qrcode({width: 124,height: 124,text: \"size doesn't matter\"});");
                }
            }, 5, TimeUnit.SECONDS);

        }

        //add(QRCode);

    }

    public static List<String> ipAddress() {
        List<String> ips = new ArrayList<>();
        try {
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while(e.hasMoreElements()) {
                NetworkInterface n = (NetworkInterface) e.nextElement();
                Enumeration ee = n.getInetAddresses();
                while (ee.hasMoreElements()) {
                    InetAddress i = (InetAddress) ee.nextElement();
                    String addr = i.getHostAddress();
                    if (addr.contains("192.168.") && !addr.contains("56.1")) {
                        ips.add(addr + ":" + Spark.port());
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return ips;
    }

}
