package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.Query;
import tk.leoforney.passcheckerserver.Car;
import tk.leoforney.passcheckerserver.PassManagement;
import tk.leoforney.passcheckerserver.Student;

import java.util.List;

public class CarEditor extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    Dialog dialog;
    String plateNumberString;
    Car foundCar;
    PassManagement passManagement = PassManagement.getInstance();
    TextField plateNumber, make, model, year, color;
    ComboBox<Student> studentComboBox;
    private Grid<Student> grid;
    private List<Student> studentList;
    private PassView passView;
    private Student foundStudent;

    public CarEditor() {
        dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        setPadding(true);

        VerticalLayout layout = new VerticalLayout();

        FormLayout form = new FormLayout();

        plateNumber = new TextField("Plate Number");
        make = new TextField("Make");
        model = new TextField("Model");
        year = new TextField("Year");
        setType(year, "number");
        color = new TextField("Color");
        studentComboBox = new ComboBox<>();
        studentComboBox.setRequired(true);
        studentComboBox.setLabel("Owner");
        studentComboBox.setItemLabelGenerator((ItemLabelGenerator<Student>) item -> item.name + " " + item.id);

        form.add(plateNumber, make, model, year, color, studentComboBox);
        layout.add(form);

        Button saveButton = new Button("Save", event -> {
            dialog.close();
        });
        saveButton.getElement().getThemeList().add("small");
        saveButton.setId("SaveCar");
        saveButton.addClickListener(this);

        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });
        cancelButton.getElement().getThemeList().add("small");
        cancelButton.getElement().getThemeList().add("tertiary");

        dialog.add(layout, new Span(saveButton, cancelButton));
    }

    String openIntent = "";

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        String buttonId = "";
        if (event.getSource().getId().isPresent()) {
            buttonId = event.getSource().getId().get();
        }

        if (buttonId.equals("OpenNewCarEditorDialog")) {
            openIntent = "new";
            dialog.open();
            plateNumber.setValue("");
            make.setValue("");
            model.setValue("");
            year.setValue("");
            color.setValue("");

        }

        if (buttonId.equals("OpenCarEditorDialog")) {
            openIntent = "edit";
            dialog.open();
            event.getSource().getParent().ifPresent(component -> {
                Html label = (Html) component.getChildren().findFirst().get();
                plateNumberString = label.getInnerHtml();
                foundCar = passManagement.findCarByPlateNumber(plateNumberString);

                for (Student student : studentList) {
                    if (student.id == foundCar.id) {
                        foundStudent = student;
                    }
                }

                plateNumber.setValue(foundCar.plateNumber);
                make.setValue(foundCar.make);
                model.setValue(foundCar.model);
                year.setValue(String.valueOf(foundCar.year));
                color.setValue(foundCar.color);
                studentComboBox.setValue(foundStudent);

            });
        }
        if (buttonId.equals("SaveCar")) {
            Car car = new Car(plateNumber.getValue(), color.getValue(), make.getValue(), model.getValue(), Integer.valueOf(year.getValue()), studentComboBox.getValue().id);
            if (openIntent.equals("edit")) {
                passManagement.updateCarFromPlateNumber(plateNumberString, car);
            } else if (openIntent.equals("new")) {
                passManagement.createCar(car);
                if (passView.studentView) {

                }


                System.out.println(passView.studentList.get(0).name);

                for (Student student : passView.studentList) {
                    if (student.id == foundCar.id) {
                        foundStudent = student;
                    }
                }
                foundStudent.getCars().add(car);
            }

            List<Car> studentCars = foundStudent.cars;
            for (Car iteratedCar : studentCars) { // Iterate through student cars and update the same one
                if (iteratedCar.plateNumber.equals(plateNumberString)) {
                    studentCars.set(studentCars.indexOf(iteratedCar), car);
                }
            }
            grid.getDataProvider().refreshItem(foundStudent);

        }
    }

    void setGrid(Grid<Student> grid) {
        this.grid = grid;
    }

    void setList(List<Student> list) {
        this.studentList = list;
        studentComboBox.setItems(studentList);
    }

    void setPassView(PassView passView) {
        this.passView = passView;
    }

    private static void setType(TextField textField, String type) {
        textField.getElement().getNode().runWhenAttached(ui -> {
            ui.getPage().executeJavaScript("$0.focusElement.type=$1", textField,
                    type);
        });
    }
}
