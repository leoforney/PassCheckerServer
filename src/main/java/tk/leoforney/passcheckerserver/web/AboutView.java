package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class AboutView extends VerticalLayout {

    public AboutView() {
        Component about = new Span(new H3("Page title"), new Span("About"));
        add(about);
    }

}
