package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
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

        // Set the root layout for the UI
        VerticalLayout content = new VerticalLayout();
        add(content);

        HorizontalLayout titleBar = new HorizontalLayout();
        titleBar.setWidth("100%");
        content.add(titleBar);

        Label title = new Label("The Ultimate Cat Finder");
        titleBar.add(title);

        Label titleComment = new Label("for Vaadin");
        titleComment.setSizeUndefined(); // Take minimum space
        titleBar.add(titleComment);


        Label hello = new Label("Hello Gradle app!");
        add(hello);

        Button button = new Button("Click me", event -> {
            hello.setText("Clicked!");
            hello.setClassName("clicked");
        });
        add(button);
    }
}