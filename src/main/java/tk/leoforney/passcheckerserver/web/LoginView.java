package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.binder.Validator;
import com.vaadin.flow.data.binder.ValueContext;
import com.vaadin.flow.data.validator.BeanValidator;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.apache.commons.codec.binary.Base64;
import org.vaadin.marcus.shortcut.Shortcut;

import java.io.UnsupportedEncodingException;

import static tk.leoforney.passcheckerserver.UserManagement.authenticated;


@Route("login")
@PageTitle("PassChecker - Login")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class LoginView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>>, KeyNotifier {

    TextField email;
    PasswordField password;
    Base64 base64;

    Binder<Account> binder = new Binder<>(Account.class);

    public LoginView() {

        base64 = new Base64();
        VerticalLayout loginLayout = new VerticalLayout();

        H2 title = new H2("PassCheckerServer");
        add(title);

        FormLayout form = new FormLayout();

        email = new TextField();
        email.setPlaceholder("joe@wths.net");
        form.addFormItem(email, "Email");
        binder.forField(email)
                .withValidator(
                        new EmailValidator("This doesn't look like a valid email address"))
                .bind(Account::getEmail, Account::setEmail);

        password = new PasswordField();
        form.addFormItem(password, "Password");
        binder.forField(password)
                .withValidator(passwordValidator)
                .bind(Account::getPassword, Account::setPassword);

        Button button = new Button("Login");
        button.addClickListener(this);
        loginLayout.add(form, button);

        Shortcut.add(password, Key.ENTER, button::click);
        Shortcut.add(email, Key.ENTER, button::click);

        add(loginLayout);

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

    private void login() {
        String emailString = email.getValue();
        String passwordString = password.getValue();

        String concatString = emailString + ":" + passwordString;
        String token = "";

        try {
            token = new String(base64.encode(concatString.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        if (authenticated(token)) {
            VaadinSession.getCurrent().setAttribute("Token", token);
            this.getUI().ifPresent(ui -> ui.navigate(""));
        } else {
            Notification.show("Invalid email or password").setDuration(3000);
        }
    }
}