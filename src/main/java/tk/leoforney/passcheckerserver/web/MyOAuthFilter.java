package tk.leoforney.passcheckerserver.web;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import org.riversun.oauth2.google.OAuthFilter;
import org.riversun.oauth2.google.OAuthSecrets;

import javax.servlet.annotation.WebFilter;
import java.io.*;
import java.util.Arrays;
import java.util.List;

import static tk.leoforney.passcheckerserver.Main.wd;
import static tk.leoforney.passcheckerserver.web.MyAppServlet.JSON_FACTORY;

@WebFilter(urlPatterns = {"/import", "/import/google"})
public class MyOAuthFilter extends OAuthFilter {

    public MyOAuthFilter() {
        File credentialsFile = new File(wd + File.separator + "client_secrets.json");

        InputStream in = null;
        try {
            in = new FileInputStream(credentialsFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
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
	protected String getAuthRedirectUrl() {
		return ServletCallbackSample.OAUTH2_CALLBACK_URL;
	}

	@Override
	protected boolean isAuthenticateEverytime() {
		return false;
	}

	// Return OAuth2 scope you want to be granted to by users
	@Override
	protected List<String> getScopes() {
        
		final String OAUTH2_SCOPE_USERINFO_PROFILE = "https://www.googleapis.com/auth/spreadsheets.readonly";
		final String OAUTH2_SCOPE_USERINFO_GPLUS = "https://www.googleapis.com/auth/userinfo.profile";
		final String OAUTH2_SCOPE_EMAIL = "email";
		final String OAUTH2_SCOPE_DRIVE = "https://www.googleapis.com/auth/drive.readonly";

		return Arrays.asList(OAUTH2_SCOPE_USERINFO_PROFILE, OAUTH2_SCOPE_USERINFO_GPLUS, OAUTH2_SCOPE_EMAIL, OAUTH2_SCOPE_DRIVE);

	}

}