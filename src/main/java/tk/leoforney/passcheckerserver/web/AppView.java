package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.HasText;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.applayout.MenuItemClickEvent;
import com.vaadin.flow.component.cookieconsent.CookieConsent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinService;
import com.vaadin.flow.server.VaadinSession;
import tk.leoforney.passcheckerserver.User;
import tk.leoforney.passcheckerserver.UserManagement;

import javax.servlet.http.Cookie;
import java.util.ArrayList;
import java.util.List;

import static tk.leoforney.passcheckerserver.Runner.show;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * The main view of the application
 */
@Route(value = "", layout = MainLayout.class)
public class AppView extends AppLayout implements ComponentEventListener<MenuItemClickEvent> {

    List<AppLayoutMenuItem> menuItems;
    AppLayoutMenu menu;

    public AppView() {

        User user = checkAuthentication(AppView.this);

        if (user != null) {
            show("Welcome to PassCheckerServer " + user.getFirstName() + "!");

            CookieConsent dialog = new CookieConsent(
                    "We are using cookies to make your visit here awesome!",
                    "Cool!", "Why?", "/about",
                    CookieConsent.Position.BOTTOM_LEFT);

            menuItems = new ArrayList<>();
            menuItems.add(0, new AppLayoutMenuItem(VaadinIcon.HOME.create(), "Home", this));
            menuItems.add(1, new AppLayoutMenuItem(VaadinIcon.KEY.create(), "Passes", this));
            menuItems.add(2, new AppLayoutMenuItem(VaadinIcon.USER.create(), "Users", this));
            menuItems.add(3, new AppLayoutMenuItem(VaadinIcon.INFO.create(), "About", this));
            menuItems.add(4, new AppLayoutMenuItem(VaadinIcon.SIGN_OUT.create(),
                    "Sign Out", this));

            Label title = new Label("PassCheckerServer");

            setBranding(title);
            menu = this.createMenu();

            for (AppLayoutMenuItem item : menuItems) {
                menu.addMenuItem(item);
            }

            menu.selectMenuItem(menuItems.get(0));

        }

    }

    public static void setTitle(Component component, String title) {
        UI.getCurrent().access((Command) () -> {
            if (component.getUI().isPresent()) {
                component.getUI().get().getPage().setTitle("PCS - " + title);
            }
        });
    }

    public static void setTitle(Component component) {
        setTitle(component, ((HasText) component).getText());
    }

    public static User checkAuthentication(Component component) {
        User foundUser = new User("", "");
        Object tokenObject = VaadinSession.getCurrent().getAttribute("Token");

        if (tokenObject == null || !authenticated(String.valueOf(tokenObject))) {
            UI.getCurrent().access((Command) () -> {
                if (component.getUI().isPresent()) {
                    component.getUI().get().getPage().executeJavaScript("window.location.href='login'");
                }
            });
        } else if (authenticated(String.valueOf(tokenObject))) {
            foundUser = UserManagement.getInstance().userFromToken(String.valueOf(tokenObject));
        }
        return foundUser;
    }

    public static Cookie getCookieByName(final String name) {
        // Fetch all cookies from the request
        Cookie[] cookies = VaadinService.getCurrentRequest().getCookies();

        // Iterate to find cookie by its name
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie;
            }
        }
        return null;
    }

    public static void destroyCookieByName(final String name) {
        Cookie cookie = getCookieByName(name);

        if (cookie != null) {
            cookie.setValue(null);
            // By setting the cookie maxAge to 0 it will deleted immediately
            cookie.setMaxAge(0);
            cookie.setPath("/");
            VaadinService.getCurrentResponse().addCookie(cookie);
        }
    }

    @Override
    public void onComponentEvent(MenuItemClickEvent event) {
        checkAuthentication(AppView.this);
        switch (event.getSource().getTitle().toUpperCase()) {
            case "HOME":
                this.removeContent();
                this.setContent(new HomeView());
                menu.selectMenuItem(menuItems.get(0));
                break;
            case "ABOUT":
                this.removeContent();
                this.setContent(new AboutView());
                menu.selectMenuItem(menuItems.get(3));
                break;
            case "USERS":
                this.removeContent();
                this.setContent(new UsersView());
                menu.selectMenuItem(menuItems.get(2));
                break;
            case "PASSES":
                this.removeContent();
                this.setContent(new PassView());
                menu.selectMenuItem(menuItems.get(1));
                break;
            case "SIGN OUT":
                VaadinSession.getCurrent().close();
                destroyCookieByName("Token");
                getUI().ifPresent(ui -> ui.getPage().executeJavaScript("window.location.href='login'"));
                this.removeContent();
                break;
        }
    }


}