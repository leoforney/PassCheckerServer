package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.StreamResource;
import tk.leoforney.passcheckerserver.ImageInputStream;

import java.io.File;

import static tk.leoforney.passcheckerserver.PhotoManagement.uploadDir;
import static tk.leoforney.passcheckerserver.web.AppView.checkAuthentication;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

public class HomeView extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    private Image latestPhoto;
    private VerticalLayout layout;

    public HomeView() {

        H2 title = new H2("Home");
        setTitle(title);
        add(title);
        checkAuthentication(this);

        latestPhoto = new Image();
        latestPhoto.setSrc("http://localhost:4567/latest.jpg");
        Button button = new Button(VaadinIcon.REFRESH.create());
        button.addClickListener(this);
        add(new Span(new H4("Latest Photo"), button));
        layout = new VerticalLayout();
        layout.add(latestPhoto);
        add(layout);
    }

    public void updateImage() {
        UI.getCurrent().access((Command) () -> {
            latestPhoto = new Image();
            File file = new File(uploadDir.getAbsolutePath() + File.separator + "latest.jpg");
            ImageInputStream imageInputStream = new ImageInputStream(file);
            StreamResource resource = new StreamResource("latest.jpg", imageInputStream);
            latestPhoto.setSrc(resource);
            layout.removeAll();
            layout.add(latestPhoto);
            imageInputStream.closeAllStreams();
        });
    }


    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        updateImage();
        System.out.println("Image Updated");
    }

}
