package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(value = Lumo.class, variant = Lumo.DARK)
@Route(value = "scanLog")
@StyleSheet("https://fonts.googleapis.com/css?family=Inconsolata")
public class LogView extends VerticalLayout implements PageConfigurator {

    private static final Logger logger = Logger.getLogger(LogView.class.getName());

    public LogView() {
        setId("SomeView");
        setTitle(this, "Log");
        add(new H2("Scan log"));
    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addLink("shortcut icon", "icons/favicon.ico");
        settings.addFavIcon("icon", "icons/icon-192.png", "192x192");
        String script = "window.onbeforeunload = function (e) { var e = e || window.event; document.getElementById(\"SomeView\").$server.browserIsLeaving(); return; };";
        settings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
    }

    @ClientCallable
    public void browserIsLeaving() {
        logger.log(Level.INFO, "Called browserIsLeaving");
    }

}
