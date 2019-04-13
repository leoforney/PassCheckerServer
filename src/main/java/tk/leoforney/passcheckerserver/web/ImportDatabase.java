package tk.leoforney.passcheckerserver.web;

import com.github.javafaker.Faker;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServletRequest;
import org.riversun.oauth2.google.OAuthSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tk.leoforney.passcheckerserver.FakeCarInformation;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.api.client.googleapis.javanet.GoogleNetHttpTransport.newTrustedTransport;
import static tk.leoforney.passcheckerserver.Runner.show;

@Route("import")
public class ImportDatabase extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    private final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    Logger logger = LoggerFactory.getLogger(ImportDatabase.class);
    private Drive drive;
    private Sheets sheets;
    private ComboBox<File> comboBox;
    private Label sheetId;
    Faker faker;

    public ImportDatabase() throws IOException {

        H3 title = new H3("Import existing Google Sheets to PCS");
        add(title);

        HttpServletRequest req = VaadinServletRequest.getCurrent();
        System.out.println(req.getRequestedSessionId());

        GoogleCredential credential = OAuthSession.getInstance().createCredential(req);
        // Get unique userId
        String userId = OAuthSession.getInstance().getUserId(req);

        Oauth2 oauth2 = new Oauth2.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                new com.google.api.client.json.jackson2.JacksonFactory(),
                credential).build();

        try {
            final NetHttpTransport HTTP_TRANSPORT = newTrustedTransport();
            drive = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .setApplicationName("PassCheckerServer")
                    .build();
            sheets = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
                    .build();
        } catch (Exception e) {
            show(e.getMessage());
        }


        // Get userInfo using credential
        try {
            Userinfoplus userInfo = oauth2.userinfo().get().execute();
            add(new Label("Current user: " + userInfo.getName()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        sheetId = new Label();
        add(sheetId);

        // Print the names and IDs for up to 100 sheets
        FileList result = drive.files().list()
                .setPageSize(100)
                .setQ("mimeType='application/vnd.google-apps.spreadsheet'")
                .setFields("nextPageToken, files(id, name)")
                .execute();
        List<File> files = result.getFiles();
        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            List<File> passFiles = new ArrayList<>();
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getName(), file.getId());
                if (file.getName().toLowerCase().contains("pass")) {
                    passFiles.add(file);
                }
            }
            for (File file: passFiles) {
                files.remove(file);
                files.add(0, file);
            }
        }

        comboBox = new ComboBox<>();
        comboBox.setSizeFull();
        comboBox.setLabel("Select File");
        comboBox.setItemLabelGenerator((ItemLabelGenerator<File>) File::getName);

        if (files != null && !files.isEmpty()) {
            comboBox.setItems(files);
        }

        comboBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<File>, File>>) event -> {
            if (!event.getHasValue().isEmpty()) {
                selectedFile = event.getValue();
                sheetId.setText(selectedFile.getId());
            }
        });

        add(comboBox);

        Button fetchButton = new Button("Fetch spreadsheet");
        fetchButton.addClickListener(this);
        add(fetchButton);

    }

    private File selectedFile;

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {

        if (selectedFile != null) {
            final String spreadsheetId = selectedFile.getId();
            final String range = "Sheet2!A2:D";
            ValueRange response = null;
            try {
                response = sheets.spreadsheets().values()
                        .get(spreadsheetId, range)
                        .execute();
            } catch (IOException e) {
                e.printStackTrace();
            }
            List<List<Object>> values = response.getValues();
            if (values == null || values.isEmpty()) {
                System.out.println("No data found.");
            } else {
                //System.out.println("Name, Major");
                for (List row : values) {
                    if (row.isEmpty()) {
                        break;
                    }
                    int size = row.size() - 1;
                    for (int i = 0; i < size; i++) {
                        if (row.get(i).equals("")) size--;
                    }
                    System.out.println("Amount of plates: " + size);
                    if (row.size() > 1) {
                        if (row.size() == 2) {
                            System.out.println(row.get(0) + " : " + row.get(1));
                            FakeCarInformation fci = FakeCarInformation.getInstance();
                        } else if (row.size() == 3) {
                            System.out.println(row.get(0) + " : " + row.get(1) + " : " + row.get(2));
                        } else if (row.size() == 4) {
                            System.out.println(row.get(0) + " : " + row.get(1) + " : " + row.get(2) + " : " + row.get(3));
                        }
                    }

                    // Print columns A and E, which correspond to indices 0 and 4.
                }
            }
        } else {
            show("Please select a file");
        }

    }
}
