package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.AppLayoutMenu;
import com.vaadin.flow.component.applayout.AppLayoutMenuItem;
import com.vaadin.flow.component.applayout.MenuItemClickEvent;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import tk.leoforney.passcheckerserver.User;
import tk.leoforney.passcheckerserver.UserManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * The main view of the application
 */
@Route("")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class AppView extends AppLayout implements ComponentEventListener<MenuItemClickEvent> {

    List<AppLayoutMenuItem> menuItems;
    AppLayoutMenu menu;

    public AppView() {

        menuItems = new ArrayList<>();
        menuItems.add(new AppLayoutMenuItem(VaadinIcon.HOME.create(), "Home", this));
        menuItems.add(new AppLayoutMenuItem(VaadinIcon.USER.create(), "Users", this));
        menuItems.add(new AppLayoutMenuItem(VaadinIcon.INFO.create(), "About", this));
        menuItems.add(new AppLayoutMenuItem(VaadinIcon.KEY.create(), "Passes", this));
        menuItems.add(new AppLayoutMenuItem(VaadinIcon.SIGN_OUT.create(),
                "Sign Out", this));

        setBranding(new Label("PassCheckerServer"));
        menu = this.createMenu();

        for (AppLayoutMenuItem item : menuItems) {
            menu.addMenuItem(item);
        }

        menu.selectMenuItem(menuItems.get(0));

        User user = checkAuthentication(AppView.this);
        Notification.show("Welcome to PassCheckerServer " + user.name);

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
                menu.selectMenuItem(menuItems.get(2));
                break;
            case "USERS":
                this.removeContent();
                this.setContent(new UsersView());
                menu.selectMenuItem(menuItems.get(1));
                break;
            case "PASSES":
                this.removeContent();
                this.setContent(new PassesView());
                menu.selectMenuItem(menuItems.get(3));
                break;
            case "SIGN OUT":
                VaadinSession.getCurrent().close();
                getUI().ifPresent(ui -> ui.getPage().executeJavaScript("window.location.href='login'"));
                this.removeContent();
                break;
        }
    }

    public static User checkAuthentication(Component component) {
        User foundUser = new User("", "");
        Object tokenObject = VaadinSession.getCurrent().getAttribute("Token");

        if (tokenObject == null || !authenticated(String.valueOf(tokenObject))) {
            if (component.getUI().isPresent()) {
                UI.getCurrent().access((Command) () -> {
                    component.getUI().get().getPage().executeJavaScript("window.location.href='login'");
                });
            }
        } else if (authenticated(String.valueOf(tokenObject))) {
            foundUser = UserManagement.getInstance().userFromToken(String.valueOf(tokenObject));
        }
        return foundUser;
    }

}