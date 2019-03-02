package tk.leoforney.passcheckerserver.web;

import com.google.gson.Gson;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import tk.leoforney.passcheckerserver.PassManagement;
import tk.leoforney.passcheckerserver.Runner;
import tk.leoforney.passcheckerserver.Student;

import static tk.leoforney.passcheckerserver.Runner.show;

public class NewStudentEditor extends VerticalLayout implements ComponentEventListener<ClickEvent<Button>> {

    Dialog dialog;
    PassManagement passManagement = PassManagement.getInstance();
    TextField id, name;
    private PassView passView;
    private Grid<Student> grid;
    private Gson gson;

    public NewStudentEditor() {
        dialog = new Dialog();

        dialog.setCloseOnEsc(false);
        dialog.setCloseOnOutsideClick(false);

        setPadding(true);

        VerticalLayout layout = new VerticalLayout();

        layout.add(new H3("Add new student"));

        id = new TextField("Student ID");
        id.setPlaceholder("190333");
        setType(id, "number");
        name = new TextField("Student Name");
        name.setPlaceholder("John Doe");

        FormLayout form = new FormLayout();
        form.add(id, name);
        layout.add(form);

        Button saveButton = new Button("Add", event -> {
            dialog.close();
        });
        saveButton.getElement().getThemeList().add("small");
        saveButton.setId("SaveNewStudent");
        saveButton.addClickListener(this);

        Button cancelButton = new Button("Cancel", event -> {
            dialog.close();
        });
        cancelButton.getElement().getThemeList().add("small");
        cancelButton.getElement().getThemeList().add("tertiary");

        dialog.add(layout, new Span(saveButton, cancelButton));
    }

    public void setPassView(PassView passView) {
        this.passView = passView;
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        if (event.getSource().getId().get().equals("OpenNewStudentEditorDialog")) {
            dialog.open();
            id.setValue("");
            name.setValue("");
        }
        if (event.getSource().getId().get().equals("SaveNewStudent")) {
            Student student = new Student();
            student.setId(Integer.valueOf(id.getValue()));
            String[] split = name.getValue().split("\\s+");
            String firstName = split[0].substring(0, 1).toUpperCase()
                    + split[0].substring(1).toLowerCase();
            String lastName = split[1].substring(0, 1).toUpperCase()
                    + split[1].substring(1).toLowerCase();
            student.setName(firstName + " " + lastName);
            String response = passManagement.createStudent(student);
            show(response);
            passView.studentList.add(student);
            passView.studentGrid.getDataProvider().refreshAll();
        }
    }

    void setGrid(Grid<Student> grid) {
        this.grid = grid;
    }

    private static void setType(TextField textField, String type) {
        textField.getElement().getNode().runWhenAttached(ui -> {
            ui.getPage().executeJavaScript("$0.focusElement.type=$1", textField,
                    type);
        });
    }
}
