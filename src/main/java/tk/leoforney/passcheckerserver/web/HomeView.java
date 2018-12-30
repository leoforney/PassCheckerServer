package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static tk.leoforney.passcheckerserver.web.AppView.checkAuthentication;

public class HomeView extends VerticalLayout {

    public HomeView() {
        H2 title = new H2("Home");
        add(title);
        checkAuthentication(this);
    }

}
