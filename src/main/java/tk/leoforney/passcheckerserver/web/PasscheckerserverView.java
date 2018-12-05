package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.router.Route;

/**
 * The main view of the application
 */
@Route("")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class PasscheckerserverView extends VerticalLayout {

    public PasscheckerserverView() {
        setClassName("app-view");

        Label hello = new Label("Hello Gradle app!");
        add(hello);

        Button button = new Button("Click me", event -> {
            hello.setText("Clicked!");
            hello.setClassName("clicked");
        });
        add(button);
    }
}