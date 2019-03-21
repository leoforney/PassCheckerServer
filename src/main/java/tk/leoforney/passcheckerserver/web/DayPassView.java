package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.OptionalParameter;
import com.vaadin.flow.router.Route;

import static tk.leoforney.passcheckerserver.Runner.show;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route(value = "dayPass", layout = MainLayout.class)
public class DayPassView extends VerticalLayout implements HasUrlParameter<String> {

    int studentId;

    @Override
    public void setParameter(BeforeEvent event,
                             @OptionalParameter String parameter) {
        if (parameter == null) {
            show("Welcome anonymous.");
        } else {
            if (parameter.matches("^(\\+|-)?\\d+$")) {
                studentId = Integer.valueOf(parameter);
            }
            show(String.format("Welcome %s.", parameter));
        }

    }

    public DayPassView() {
        setTitle(this, "Day Pass");

        setAlignItems(Alignment.CENTER);

        Div loginPanel = new Div();

        HorizontalLayout hLayout = new HorizontalLayout();
        hLayout.setAlignItems(Alignment.CENTER);

        Image logo = new Image();
        logo.setSrc("https://www.d121.org/cms/lib/IL02214492/Centricity/Template/GlobalAssets/images///logo/Warren-Floor-W-Crest_150px.png");
        hLayout.add(logo);

        VerticalLayout loginTextFields = new VerticalLayout();

        TextField studentNameField = new TextField("Student Name");
        TextField studentIdField = new TextField("Student ID");
        Button submitButton = new Button("Submit");

        loginTextFields.add(studentNameField, studentIdField, submitButton);
        hLayout.add(loginTextFields);

        loginPanel.add(hLayout);
        add(loginPanel);
    }

}
