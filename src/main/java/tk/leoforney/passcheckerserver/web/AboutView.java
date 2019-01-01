package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

public class AboutView extends VerticalLayout {

    public AboutView() {
        H2 title = new H2("About");
        add(title);
        setTitle(title);
    }

}
