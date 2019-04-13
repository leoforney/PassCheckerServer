package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.*;

/**
 * Created by Leo on 5/5/2018.
 */
@RestController
public class UserManagement {

    Gson gson;
    private Connection connection;
    private final static String PATH = "/user";

    private static UserManagement instance = null;

    public static UserManagement getInstance() {
        if (instance == null) {
            instance = new UserManagement();
        }
        return instance;
    }

    private UserManagement() {
        gson = new Gson();
        connection = SpringUtils.connection;
    }

    public static boolean authenticated(Request request) {
        return authenticated(request.headers("Token"));
    }

    public static boolean authenticated(String token) {
        List<String> names;
        try {
            Statement statement = Runner.connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from Users WHERE Token = \"" + token + "\"");

            names = new ArrayList<>();
            while (rs.next()) {
                // read the result set
                names.add(rs.getString("Name"));
            }

            rs.close();

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            names = new ArrayList<>();
        }

        if (names.size() == 1) {
            return true;
        } else {
            return false;
        }
    }

    String createUser(User user) {
        String executionStatement = "INSERT INTO Users (Name, Token) VALUES (\'" + user.name + "\', '" + user.token + "')";

        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(executionStatement);
            statement.execute();
            connection.commit();
            statement.close();
            return "Created user successfully";
        } catch (SQLException e) {
            return "Failed to create user";
        }

    }

    String deleteUser(String name) {
        String executionStatement = "DELETE FROM Users WHERE Name = \"" + name + "\"";

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(executionStatement);
            statement.execute();
            connection.commit();
            if (statement != null) {
                statement.close();
            }
            return "Deleted user successfully";
        } catch (SQLException e) {
            return "Failed to delete user";
        }
    }

    public User userFromToken(String token) {
        List<User> users;
        try {
            Statement statement = Runner.connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from Users WHERE Token = \"" + token + "\"");

            users = new ArrayList<>();
            while (rs.next()) {
                // read the result set
                users.add(new User(rs.getString("Name"), rs.getString("Token")));
            }

            rs.close();

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            users = new ArrayList<>();
        }

        if (users.size() == 1) {
            return users.get(0);
        } else {
            return new User("", "");
        }
    }

    public List<User> usersList() {
        List<User> users;
        try {
            Statement statement = Runner.connection.createStatement();

            ResultSet rs = statement.executeQuery("select * from Users");

            users = new ArrayList<>();
            while (rs.next()) {
                // read the result set
                users.add(new User(rs.getString("Name"), rs.getString("Token")));
            }

            rs.close();

            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
            users = new ArrayList<>();
        }
        return users;
    }

    public static final String[] PATHS = {
            PATH,
            PATH + "/validateuser/json"};

    /*

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String getStudentName(@RequestHeader(value = "Token") String token) {
        String response = "";
        if (authenticated(token)) {

            response =
        } else {
            response = "403";
        }
        return response;
    }*/

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String createUser(@RequestHeader(value = "Token") String token,
                             @RequestBody String body) {
        if (authenticated(token)) {
            User user = gson.fromJson(body, User.class);
            if (user.isValid()) {
                return createUser(user);
            } else {
                return "Either the name or the token was not entered correctly";
            }
        } else {
            return "Error: 403";
        }
    }

    @RequestMapping(value = "/user/validateuser/json", method = RequestMethod.GET)
    public String validateUser(@RequestHeader(value = "Token") String token) {
        Statement statement = null;
        try {
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        System.out.println("Trying to find " + token);

        ResultSet rs = null;
        try {
            rs = statement.executeQuery("SELECT * FROM Users");
        } catch (SQLException e) {
            e.printStackTrace();
        }

        List<User> userList = new ArrayList<>();
        User correctUser = null;
        try {
            while (rs.next()) {
                User newUser = new User(rs.getString("Name"), rs.getString("Token"));
                System.out.println(newUser.token + " : " + newUser.name);
                if (newUser.isValid() && newUser.token.equals(token)) {
                    if (correctUser == null) {
                        correctUser = newUser;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        userList.clear();
        if (correctUser != null) {
            userList.add(correctUser);
        }

        try {
            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        if (userList.size() == 1) {
            return gson.toJson(userList.get(0));
        }

        return "";

    }

    /*
    void registerHooks() {

        get(PATH, (request, response) -> {
            if (authenticated(request)) {
                Statement statement = connection.createStatement();

                ResultSet rs = statement.executeQuery("select * from Users");

                StringBuilder sb = new StringBuilder();
                while (rs.next()) {
                    // read the result set
                    sb.append("Name: ").append(rs.getString("Name")).append(", ");
                    sb.append("Token: ").append(rs.getString("Token")).append("\n");
                }

                response.body(sb.toString());

                rs.close();
            } else {
                response.status(403);
            }
            return response.body();
        });

        delete(PATH, (request, response) -> {
            if (authenticated(request)) {
                return deleteUser(request.headers("Name"));
            } else {
                response.status(403);
            }
            return response.body();
        });
    }*/

}
