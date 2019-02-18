package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.server.Command;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import tk.leoforney.passcheckerserver.PhotoManagement;

import java.io.File;

import static tk.leoforney.passcheckerserver.web.AppView.checkAuthentication;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

public class HomeView extends VerticalLayout implements FileAlterationListener {

    Image latestPhoto;

    public HomeView() {
        H2 title = new H2("Home");
        setTitle(title);
        add(title);
        checkAuthentication(this);

        add(new H4("Latest Photo"));
        latestPhoto = new Image();
        File latestPhotoFile = new File(PhotoManagement.uploadDir.getAbsolutePath() + File.separator + "latest.jpg");
        FileAlterationObserver observer = new FileAlterationObserver(latestPhotoFile);
        FileAlterationMonitor monitor = new FileAlterationMonitor(500);
        monitor.addObserver(observer);
        try {
            monitor.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        observer.addListener(this);
        latestPhoto.setSrc("http://localhost:4567/latest.jpg");
        add(latestPhoto);
    }

    @Override
    public void onStart(FileAlterationObserver observer) {

    }

    @Override
    public void onDirectoryCreate(File directory) {

    }

    @Override
    public void onDirectoryChange(File directory) {

    }

    @Override
    public void onDirectoryDelete(File directory) {

    }

    @Override
    public void onFileCreate(File file) {

    }

    @Override
    public void onFileChange(File file) {
        if (file.getName().equals("latest.jpg")) {
            UI.getCurrent().access((Command) () -> latestPhoto.setSrc("http://localhost:4567/latest.jpg"));
            System.out.println("File should be refreshed");
        }
    }

    @Override
    public void onFileDelete(File file) {

    }

    @Override
    public void onStop(FileAlterationObserver observer) {

    }
}
