package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.BeanValidator;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinResponse;
import com.vaadin.flow.server.VaadinSession;
import org.apache.commons.codec.binary.Base64;
import org.vaadin.marcus.shortcut.Shortcut;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;

import static tk.leoforney.passcheckerserver.UserManagement.authenticated;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;


@Route(value = "login", layout = MainLayout.class)
public class LoginView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>>, KeyNotifier {

    TextField email;
    PasswordField password;
    Checkbox rememberMe;
    Base64 base64;

    Binder<Account> binder = new Binder<>(Account.class);

    public LoginView() {
        setTitle(this, "Login");

        base64 = new Base64();
        VerticalLayout loginLayout = new VerticalLayout();

        H2 title = new H2("PassCheckerServer");
        add(title);

        FormLayout form = new FormLayout();

        email = new TextField();
        email.setPlaceholder("joe@wths.net");
        form.addFormItem(email, "Email");
        binder.forField(email)
                .bind(Account::getEmail, Account::setEmail);

        password = new PasswordField();
        form.addFormItem(password, "Password");
        binder.forField(password)
                .withValidator(passwordValidator)
                .bind(Account::getPassword, Account::setPassword);

        rememberMe = new Checkbox("Remember me");

        Button button = new Button("Login");
        button.addClickListener(this);
        loginLayout.add(form, rememberMe, button);

        Shortcut.add(password, Key.ENTER, button::click);
        Shortcut.add(email, Key.ENTER, button::click);

        add(loginLayout);

        /*
         * If remember me is used, login to main page
         */
        HttpServletRequest request = (HttpServletRequest) VaadinRequest.getCurrent();
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("token")) {
                    if (cookie.getValue() != null) {
                        login(cookie.getValue());
                    }
                }
            }
        }

        /*
         * If already signed in, take to main page.
         */
        UI.getCurrent().access((Command) () -> {
            Object tokenObject = VaadinSession.getCurrent().getAttribute("Token");

            if (tokenObject != null && authenticated(String.valueOf(tokenObject))) {
                if (getUI().isPresent()) {
                    getUI().get().getPage().executeJavaScript("window.location.href=''");
                }
            }
        });

    }

    private Validator<String> passwordValidator = new Validator<String>() {

        BeanValidator passwordBeanValidator = new BeanValidator(Account.class, "password");

        @Override
        public ValidationResult apply(String value, ValueContext context) {
            if (!value.isEmpty() && value.length() > 4 && value.length() < 18) {
                return passwordBeanValidator.apply(value, context);
            } else if (value.isEmpty()) {
                return ValidationResult.error("Password needs to be filled out");
            } else if (value.length() < 4) {
                return ValidationResult.error("Password too short");
            } else if (value.length() > 18) {
                return ValidationResult.error("Password too long");
            } else {
                return ValidationResult.error("There was an error. Try again");
            }
        }
    };

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        login();
    }

    private void login(String token) {
        if (rememberMe.getValue()) {
            HttpServletResponse response = (HttpServletResponse) VaadinResponse.getCurrent();
            Cookie cookie = new Cookie("Token", token);
            cookie.setMaxAge(60 * 60 * 24 * 30);
            response.addCookie(cookie);
        }

        if (authenticated(token)) {
            VaadinSession.getCurrent().getSession().setMaxInactiveInterval(60 * 60 & 3);
            VaadinSession.getCurrent().setAttribute("Token", token);
            this.getUI().ifPresent(ui -> ui.navigate(""));
        } else {
            Notification.show("Invalid email or password").setDuration(3000);
        }
    }

    private void login() {
        String emailString = email.getValue().toLowerCase();
        if (!emailString.contains("@")) {
            emailString = emailString + "@wths.net";
        }
        String passwordString = password.getValue();

        String concatString = emailString + ":" + passwordString;
        String token = "";

        try {
            token = new String(base64.encode(concatString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        login(token);
    }
}