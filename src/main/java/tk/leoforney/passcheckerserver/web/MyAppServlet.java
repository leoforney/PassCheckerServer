package tk.leoforney.passcheckerserver.web;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.riversun.oauth2.google.OAuthSecrets;
import org.riversun.oauth2.google.OAuthSession;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;

import static tk.leoforney.passcheckerserver.Main.wd;

/*
@WebServlet(
        name = "MyAppServlet",
        description = "Example Servlet Using Google OAuth2 Servlet",
        urlPatterns = {"/import/google"}
)*/
public class MyAppServlet extends HttpServlet {

    static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public MyAppServlet() {
        File credentialsFile = new File(wd + File.separator + "client_secrets.json");

        InputStream in = null;
        try {
            in = new FileInputStream(credentialsFile);
        } catch (FileNotFoundException e) {
            System.out.println("File not found: " + credentialsFile.getAbsolutePath());
        }

        GoogleClientSecrets secrets = null;
        try {
            secrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        } catch (IOException e) {
            e.printStackTrace();
        }

        OAuthSecrets.setClientSecrets(secrets);
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        System.out.println("Login servlet hit");

        GoogleCredential credential = OAuthSession.getInstance().createCredential(req);


    }

}