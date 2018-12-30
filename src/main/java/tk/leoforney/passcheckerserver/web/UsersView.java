package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class UsersView extends VerticalLayout {

    public UsersView() {
        Component users = new Span(new H3("Page title"), new Span("Users"));
        add(users);
    }

}
