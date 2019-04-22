package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.StyleSheet;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Push;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.shared.communication.PushMode;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import tk.leoforney.passcheckerserver.LogListener;
import tk.leoforney.passcheckerserver.ScanLogger;

import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(value = Lumo.class, variant = Lumo.LIGHT)
@Route(value = "scanLog")
@Push(PushMode.MANUAL)
@StyleSheet("https://fonts.googleapis.com/css?family=Inconsolata")
public class LogView extends VerticalLayout implements PageConfigurator, LogListener {

    private static final Logger logger = Logger.getLogger(LogView.class.getName());
    private ScanLogger scanLogger;
    private VerticalLayout logLayout;

    public LogView() {
        setId("SomeView");
        setTitle(this, "Log");
        add(new H2("Scan log"));
        scanLogger = ScanLogger.getInstance();
        scanLogger.addListener(this);
        logLayout = new VerticalLayout();
        logLayout.setId("consoleView");
        add(logLayout);
    }

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addInlineWithContents("#consoleView {font-family: 'Inconsolata', monospace; line-height: normal;}",
                InitialPageSettings.WrapMode.STYLESHEET);
        settings.addInlineWithContents(
                "scrollingElement = (document.scrollingElement || document.body)\n" +
                        "function scrollToBottom () {\n" +
                        "   scrollingElement.scrollTop = scrollingElement.scrollHeight;\n" +
                        "}", InitialPageSettings.WrapMode.JAVASCRIPT);
        settings.addLink("shortcut icon", "icons/favicon.ico");
        settings.addFavIcon("icon", "icons/icon-192.png", "192x192");
        String script = "window.onbeforeunload = function (e) { var e = e || window.event; document.getElementById(\"SomeView\").$server.browserIsLeaving(); return; };";
        settings.addInlineWithContents(InitialPageSettings.Position.PREPEND, script, InitialPageSettings.WrapMode.JAVASCRIPT);
    }

    @ClientCallable
    public void browserIsLeaving() {
        scanLogger.removeListener(this);
        logger.log(Level.INFO, "Called browserIsLeaving");
    }

    @Override
    public void logWritten(String string) {
        logger.log(Level.INFO, "Writting " + string + " to vaadin page log : " + getUI().isPresent());
        Label labelLog = new Label(System.currentTimeMillis() + " - " + string.toUpperCase() + "\n");
        getUI().ifPresent(ui -> ui.access((Command) () -> {
            logLayout.add(labelLog);
            ui.getPage().executeJavaScript("scrollToBottom()");
            ui.push();
        }));
    }
}
