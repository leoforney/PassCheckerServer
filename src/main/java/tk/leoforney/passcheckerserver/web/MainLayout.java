package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.router.RouterLayout;
import com.vaadin.flow.server.InitialPageSettings;
import com.vaadin.flow.server.PageConfigurator;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;

@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
@Theme(value = Lumo.class, variant = Lumo.DARK)
public class MainLayout extends Div implements RouterLayout, PageConfigurator {

    @Override
    public void configurePage(InitialPageSettings settings) {
        settings.addLink("shortcut icon", "icons/favicon.ico");
        settings.addFavIcon("icon", "icons/icon-192.png", "192x192");
    }
}