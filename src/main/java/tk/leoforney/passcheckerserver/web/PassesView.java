package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.server.VaadinSession;
import tk.leoforney.passcheckerserver.Car;
import tk.leoforney.passcheckerserver.PassManagement;

import java.util.List;

public class PassesView extends VerticalLayout {

    PassManagement passManagement;

    public PassesView() {

        passManagement = PassManagement.getInstance();

        H2 title = new H2("Passes");

        add(title);

        if (passManagement != null) {
            List<Car> carList = passManagement.getCarList();
            ListDataProvider<Car> dataProvider = new ListDataProvider<>(carList);
            for (Car car: carList) {
                System.out.println(car.toString());
            }

            Grid<Car> grid = new Grid<>();
            grid.setDataProvider(dataProvider);
            grid.setSizeFull();
            grid.setSelectionMode(Grid.SelectionMode.MULTI);

            //TextField plateNumberEditor = new TextField();

            grid.addColumn(Car::getPlateNumber)
                    .setHeader("Plate Number")
                    .setId("PlateNumberColumn");

            grid.addColumn(Car::getMake)
                    .setHeader("Make")
                    .setId("MakeColumn");

            grid.addColumn(Car::getModel)
                    .setHeader("Model")
                    .setId("ModelColumn");

            grid.addColumn(car -> Integer.toString(car.getYear()))
                    .setHeader("Year");

            grid.setDataProvider(dataProvider);
            add(grid);
        } else {
            Text text = new Text("Passes failed to load - Error getting PassManagement instance");
            add(text);
        }


    }


}
