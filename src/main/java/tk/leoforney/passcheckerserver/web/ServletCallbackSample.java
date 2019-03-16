package tk.leoforney.passcheckerserver.web;

import org.riversun.oauth2.google.OAuthCallbackServlet;

import javax.servlet.annotation.WebServlet;

@WebServlet(
        name = "GoogleLoginCallback",
        description = "Example Servlet Using Google OAuth2 Servlet",
        urlPatterns = {"/import/google/callback"}
)
@SuppressWarnings("serial")
public class ServletCallbackSample extends OAuthCallbackServlet {

  static final String OAUTH2_CALLBACK_URL = "http://localhost:8080/import/google/callback";

  @Override
  protected String getAuthRedirectUrl() {
    // Should return url of callback servlet(this servlet)
    return OAUTH2_CALLBACK_URL;
  }
}