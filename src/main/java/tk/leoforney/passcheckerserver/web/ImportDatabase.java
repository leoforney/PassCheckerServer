package tk.leoforney.passcheckerserver.web;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;
import com.vaadin.flow.server.VaadinServletConfiguration;
import com.vaadin.flow.server.VaadinServletRequest;
import org.riversun.oauth2.google.OAuthSession;

import javax.servlet.ServletConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;

@Route("import")
public class ImportDatabase extends VerticalLayout {

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public ImportDatabase() {

        H3 title = new H3("Welcome to Google Sheets import service");
        add(title);

        HttpServletRequest req = VaadinServletRequest.getCurrent();
        System.out.println(req.getRequestedSessionId());

        GoogleCredential credential = null;
        try {
            credential = OAuthSession.getInstance().createCredential(req);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Get unique userId
        String userId = OAuthSession.getInstance().getUserId(req);

        Oauth2 oauth2 = new Oauth2.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.jackson2.JacksonFactory(),
                credential).build();

        try {
            final NetHttpTransport HTTP_TRANSPORT = newTrustedTransport();
            final String spreadsheetId = "1BxiMVs0XRA5nFMdKvBdBZjgmUUqptlbs74OgvE2upms";
            final String range = "Class Data!A2:E";
            Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("PassCheckerServer")
                    .build();
            ValueRange response = service.spreadsheets().values()
                    .get(spreadsheetId, range)
                    .execute();
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
            } else {
                System.out.println("Name, Major");
                for (List row : values) {
                    // Print columns A and E, which correspond to indices 0 and 4.
                    System.out.printf("%s, %s\n", row.get(0), row.get(4));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        // Get userInfo using credential
        try {
            Userinfoplus userInfo = oauth2.userinfo().get().execute();
            add(new Label(userId));
            add(new Label(userInfo.toString()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
