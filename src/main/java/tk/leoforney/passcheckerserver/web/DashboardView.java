package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.BodySize;
import com.vaadin.flow.component.page.Viewport;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.Route;

/**
 * The main view of the application
 */
@Route("")
@BodySize(height = "100vh", width = "100vw")
@Viewport("width=device-width, minimum-scale=1.0, initial-scale=1.0, user-scalable=yes")
public class DashboardView extends VerticalLayout {

    public DashboardView() {

        setClassName("app-view");

        HorizontalLayout horizontalLayout = new HorizontalLayout();

        Tabs tabs = new Tabs();
        Tab tab1 = new Tab("Tab one");
        Tab tab2 = new Tab("Tab two");
        Tab tab3 = new Tab("Tab three");
        tabs.add(tab1, tab2, tab3);
        tabs.setSelectedTab(tab2);
        horizontalLayout.add(tabs);

        add(horizontalLayout);

    }
}