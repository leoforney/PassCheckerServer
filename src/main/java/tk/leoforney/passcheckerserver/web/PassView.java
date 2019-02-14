package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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
public class PassView extends VerticalLayout implements HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<Checkbox, Boolean>>, ComponentEventListener<ClickEvent<Button>> {

    PassManagement passManagement;
    Checkbox carView;
    VerticalLayout clearableGrid;
    CarEditor carEditor;
    Grid<Car> carGrid;
    Grid<Student> studentGrid;
    boolean studentView = false;

    public PassView() {
        checkAuthentication(this);

        passManagement = PassManagement.getInstance();

        carEditor = new CarEditor();

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

        Button deleteButton = new Button(VaadinIcon.TRASH.create());
        deleteButton.addClickListener(this);
        deleteButton.getElement().getThemeList().add("disabled");

        Button addCarButton = new Button("Add Car");
        //addCarButton.getElement().getThemeList().add("disabled");
        addCarButton.addClickListener(this);

        Button addStudentButton = new Button("Add Student");
        //addStudentButton.getElement().getThemeList().add("disabled");
        addStudentButton.addClickListener(this);

        add(new Span(addStudentButton, addCarButton, deleteButton));

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
        studentView = event.getValue();
    }

    private void loadCars() {
        List<Car> carList = passManagement.getCarList();
        carGrid = new Grid<>();
        carGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        carGrid.setItems(carList);
        carGrid.addColumn(Car::getPlateNumber).setHeader("Plate Number");
        carGrid.addColumn(Car::getYear).setHeader("Year");
        carGrid.addColumn(Car::getMake).setHeader("Make");
        carGrid.addColumn(Car::getModel).setHeader("Model");
        carGrid.addColumn(Car::getColor).setHeader("Color");
        carGrid.addColumn(Car::getId).setHeader("Student ID");
        clearableGrid.add(carGrid);
    }

    private void loadStudents() {
        List<Student> studentList = passManagement.getStudentList();
        studentGrid = new Grid<>();
        studentGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        studentGrid.setItems(studentList);
        studentGrid.addColumn(Student::getId).setHeader("ID");
        studentGrid.addColumn(Student::getName).setHeader("Name");
        studentGrid.setItemDetailsRenderer(new ComponentRenderer<>(student -> {
            VerticalLayout layout = new VerticalLayout();
            List<Car> cars = student.getCars();
            for (Car car: cars) {

                Button editButton = new Button("Edit");
                editButton.addClickListener(carEditor);
                editButton.getElement().getThemeList().add("tertiary");
                editButton.getElement().getThemeList().add("small");
                editButton.setId("OpenCarEditorDialog");

                Span span = new Span();
                Html plateNumberHtml = new Html("<b>" + car.plateNumber + "</b>");
                plateNumberHtml.setId("plateNumberBold");

                Label rest =  new Label(" " +
                        car.color + " " +
                        car.year + " " +
                        car.make + " " +
                        car.model);

                span.add(plateNumberHtml, rest, editButton);

                layout.add(span);
            }
            return layout;
        }));
        clearableGrid.add(studentGrid);
        carEditor.setGrid(studentGrid);
        carEditor.setList(studentList);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {

    }
}
