package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.router.Route;
import tk.leoforney.passcheckerserver.Car;
import tk.leoforney.passcheckerserver.PassManagement;
import tk.leoforney.passcheckerserver.Student;

import java.util.List;

import static tk.leoforney.passcheckerserver.web.AppView.checkAuthentication;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route("passes")
public class PassView extends VerticalLayout implements HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>> {

    PassManagement passManagement;
    Checkbox carView;
    VerticalLayout clearableGrid;

    public PassView() {
        checkAuthentication(this);

        passManagement = PassManagement.getInstance();

        H2 title = new H2("Passes");
        setTitle(title);
        add(title);

        carView = new Checkbox("Sort by Students");
        carView.setValue(true);
        carView.addValueChangeListener(this);
        add(carView);

        clearableGrid = new VerticalLayout();
        clearableGrid.setPadding(false);
        clearableGrid.setMargin(false);
        add(clearableGrid);

        if (passManagement != null) {
            loadStudents();
        } else {
            add(new Text("Passes failed to load - Error getting PassManagement instance"));
        }
    }


    @Override
    public void valueChanged(AbstractField.ComponentValueChangeEvent<Checkbox, Boolean> event) {
        if (event.getValue()) {
            clearableGrid.removeAll();
            loadStudents();
        } else {
            clearableGrid.removeAll();
            loadCars();
        }
    }

    private void loadCars() {
        List<Car> carList = passManagement.getCarList();
        Grid<Car> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(carList);
        grid.addColumn(Car::getPlateNumber).setHeader("Plate Number");
        grid.addColumn(Car::getYear).setHeader("Year");
        grid.addColumn(Car::getMake).setHeader("Make");
        grid.addColumn(Car::getModel).setHeader("Model");
        grid.addColumn(Car::getColor).setHeader("Color");
        grid.addColumn(Car::getId).setHeader("Student ID");
        clearableGrid.add(grid);
    }

    private void loadStudents() {
        List<Student> studentList = passManagement.getStudentList();
        Grid<Student> grid = new Grid<>();
        grid.setSelectionMode(Grid.SelectionMode.MULTI);
        grid.setItems(studentList);
        grid.addColumn(Student::getId).setHeader("ID");
        grid.addColumn(Student::getName).setHeader("Name");
        grid.setItemDetailsRenderer(new ComponentRenderer<>(student -> {
            VerticalLayout layout = new VerticalLayout();
            List<Car> cars = student.getCars();
            for (Car car: cars) {
                HorizontalLayout hLayout = new HorizontalLayout();

                Button editButton = new Button("Edit");
                editButton.getElement().getThemeList().add("tertiary");
                editButton.getElement().getThemeList().add("small");
                hLayout.add(new Span(new Html("<b>" + car.plateNumber + "</b>"), new Label(" " +
                        car.color + " " +
                        car.year + " " +
                        car.make + " " +
                        car.model), editButton));
                layout.add(hLayout);
            }
            return layout;
        }));
        clearableGrid.add(grid);
    }
}