package tk.leoforney.passcheckerserver.web;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import tk.leoforney.passcheckerserver.User;
import tk.leoforney.passcheckerserver.UserManagement;

import java.util.List;

import static tk.leoforney.passcheckerserver.web.AppView.checkAuthentication;
import static tk.leoforney.passcheckerserver.web.AppView.setTitle;

@Route("users")
public class UsersView extends VerticalLayout {

    UserManagement userManagement;

    public UsersView() {
        checkAuthentication(this);

        userManagement = UserManagement.getInstance();

        H2 title = new H2("Users");
        add(title);
        setTitle(title);

        List<User> userList = userManagement.usersList();

        Grid<User> grid = new Grid<>();
        grid.setItems(userList);

        grid.setSelectionMode(Grid.SelectionMode.SINGLE);

        grid.addColumn(User::getName).setHeader("Name");
        grid.addColumn(User::getEmail).setHeader("Email");

        add(grid);
    }

}
