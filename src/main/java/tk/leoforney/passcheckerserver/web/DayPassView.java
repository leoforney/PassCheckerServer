package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.listbox.ListBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.page.Page;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.Command;
import com.vaadin.flow.server.VaadinRequest;
import com.vaadin.flow.server.VaadinSession;
import org.vaadin.marcus.shortcut.Shortcut;
import tk.leoforney.passcheckerserver.PassManagement;
import tk.leoforney.passcheckerserver.PassType;
import tk.leoforney.passcheckerserver.SemesterUtil;
import tk.leoforney.passcheckerserver.Student;

import javax.sound.sampled.Line;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.PassType.Type.*;
import static tk.leoforney.passcheckerserver.Runner.show;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route(value = "dayPass", layout = MainLayout.class)
public class DayPassView extends VerticalLayout implements HasUrlParameter<String>, ComponentEventListener<ClickEvent<Button>> {

    int studentId;
    Student student;
    private static final Logger logger = Logger.getLogger(DayPassView.class.getName());

    // TODO: Disable school days off and holidays

    @Override
    public void setParameter(BeforeEvent event,
                             @OptionalParameter String parameter) {
        if (parameter != null) {
            if (parameter.matches("^(\\+|-)?\\d+$")) {
                studentId = Integer.valueOf(parameter);
                student = passManagement.getStudent(studentId);
                if (student != null) {
                    show(String.format("Welcome %s.", student.getFirstName()));
                    fillPassPanel();
                }
            }
        }

    }

    TextField studentNameField;
    TextField studentIdField;
    PassManagement passManagement = PassManagement.getInstance();

    VerticalLayout loginComponent;

    public DayPassView() {
        loginComponent = new VerticalLayout();
        setTitle(this, "Day Pass");
        fillLoginPanel();
        add(loginComponent);
    }

    DatePicker datePicker;
    String selectedDay;

    private void fillPassPanel() {
        loginComponent.removeAll();
        loginComponent.setAlignItems(Alignment.START);

        loginComponent.add(new H3("Parking pass for " + student.getName() + " ID: " + student.id));
        VerticalLayout dayPassLayout = new VerticalLayout();

        logger.log(Level.INFO, student.getPassType().getType().toString());

        datePicker = new DatePicker();

        ComboBox<PassType.Type> studentPassType = new ComboBox<>();
        studentPassType.setLabel("Pass Type");
        studentPassType.setItems(PassType.Type.values());
        studentPassType.setItemLabelGenerator((ItemLabelGenerator<PassType.Type>) Enum::name);
        studentPassType.setValue(student.getPassType().getType());
        studentPassType.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<PassType.Type>, PassType.Type>>) event -> {
            if (!event.getHasValue().isEmpty()) {
                show("Type changed to: " + event.getValue());
                student.getPassType().setType(event.getValue());
                passManagement.updateStudent(student);
                enableOrDisableDatePicker(datePicker, student);
            }
        });

        dayPassLayout.add(studentPassType);

        LocalDate now = LocalDate.now();

        datePicker.setMin(now);
        datePicker.setLabel("Add a day pass");

        Button addDateButton = new Button("Add");
        Button deleteButton = new Button(new Icon(VaadinIcon.TRASH));

        dayPassLayout.add(new

                HorizontalLayout(datePicker, new HorizontalLayout(addDateButton, deleteButton)));

        ListBox<String> listBox = new ListBox<>();
        listBox.setItems(student.getPassType().getDayPasses());
        listBox.addValueChangeListener((HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ListBox<String>, String>>)
                event -> selectedDay = event.getValue());

        addDateButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            String dayOfWeek = datePicker.getValue().getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.US);
            logger.log(Level.INFO, dayOfWeek);
            if (dayOfWeek.equals("Saturday") || dayOfWeek.equals("Sunday")) {
                show("Saturdays and Sundays cannot be chosen");
            } else {
                student.getPassType().addDayPass(datePicker.getValue());
                listBox.getDataProvider().refreshAll();
                passManagement.updateStudent(student);
            }
        });

        deleteButton.addClickListener((ComponentEventListener<ClickEvent<Button>>) event -> {
            if (selectedDay != null) {
                logger.log(Level.INFO, "Selected day: " + selectedDay);
                student.getPassType().deleteDayPass(selectedDay);
                listBox.getDataProvider().refreshAll();
                passManagement.updateStudent(student);
            }
        });

        dayPassLayout.add(listBox);

        loginComponent.add(dayPassLayout);
    }

    private void enableOrDisableDatePicker(DatePicker datePicker, Student student) {
        if (student.getPassType().getType().equals(FULLYEAR)) {
            datePicker.setEnabled(false);
        }
        if (student.getPassType().getType().equals(FIRSTSEMESTER)) {
            if (!SemesterUtil.getInstance().getCurrentSemester().equals(SemesterUtil.Semester.SECOND)) {
                datePicker.setEnabled(true);
            } else {
                datePicker.setEnabled(false);
            }
        }
        if (student.getPassType().getType().equals(SECONDSEMESTER)) {
            if (!SemesterUtil.getInstance().getCurrentSemester().equals(SemesterUtil.Semester.SECOND)) {
                datePicker.setEnabled(true);
            } else {
                datePicker.setEnabled(false);
            }
        }
        if (student.getPassType().getType().equals(NONE)) {
            datePicker.setEnabled(true);
        }
    }

    private void fillLoginPanel() {
        loginComponent.removeAll();
        loginComponent.setAlignItems(Alignment.CENTER);

        Div loginPanel = new Div();

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setAlignItems(Alignment.CENTER);

        Image logo = new Image();
        logo.setSrc("https://www.d121.org/cms/lib/IL02214492/Centricity/Template/GlobalAssets/images///logo/Warren-Floor-W-Crest_150px.png");
        hLayout.add(logo);

        VerticalLayout loginTextFields = new VerticalLayout();

        studentNameField = new TextField("Student Name");
        studentIdField = new TextField("Student ID");
        Button submitButton = new Button("Submit");
        Shortcut.add(loginComponent, Key.ENTER, submitButton::click);
        submitButton.addClickListener(this);

        loginTextFields.add(studentNameField, studentIdField, submitButton);
        hLayout.add(loginTextFields);

        loginPanel.add(hLayout);
        loginComponent.add(loginPanel);
    }

    @Override
    public void onComponentEvent(ClickEvent<Button> event) {
        List<Student> studentList = passManagement.getStudentList();
        Student student = null;
        for (Student studentIterated : studentList) {
            if (studentIterated.getName().toLowerCase().replace(" ", "").equals(studentNameField.getValue().replace(" ", "").toLowerCase())) {
                if (studentIterated.id == Integer.valueOf(studentIdField.getValue())) {
                    student = studentIterated;
                    Student finalStudent = student;
                    UI.getCurrent().access((Command) () -> {
                        if (getUI().isPresent()) {
                            getUI().get().getPage().executeJavaScript("window.location.href='dayPass/" + finalStudent.id + "'");
                        }
                    });
                }
            }
        }

        if (student == null) {
            show("Student not found! Please consult bookkeeper");
        }
    }
}
