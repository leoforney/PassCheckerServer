package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static spark.Spark.delete;
import static spark.Spark.get;
import static spark.Spark.post;

/**
 * Created by Leo on 5/5/2018.
 */
public class UserManagement {

    Gson gson;
    Connection connection;
    private final static String PATH = "/user";

    public UserManagement(Connection connection) {
        gson = new Gson();
        this.connection = connection;
        //System.out.println(gson.toJson(new User("Toni Forney", "dG9uaWZvcm5leTpkbDIz")));

        post(PATH, (request, response) -> {
            if (authenticated(request)) {
                String requestJson = request.body();
                User user = gson.fromJson(requestJson, User.class);
                if (user.isValid()) {
                    createUser(user, response);
                } else {
                    response.body("Either the name or the token was not entered correctly");
                }
            } else {
                response.status(403);
            }
            return response.body();
        });

        get(PATH + "validateuser/json", (request, response) -> {
            if (authenticated(request)) {
                Statement statement = connection.createStatement();

                String token = request.headers("Token");

                ResultSet rs = statement.executeQuery("SELECT * FROM Users WHERE Token=\"" + token + "\"");

                List<User> userList = new ArrayList<>();
                while (rs.next()) {
                    User newUser = new User(rs.getString("Name"), rs.getString("Token"));
                    if (newUser.isValid()) {
                        userList.add(newUser);
                    }
                }

                if (userList.size() == 1) {
                    response.body(gson.toJson(userList.get(0)));
                }

                rs.close();
            } else {
                response.status(403);
            }
            return response.body();
        });

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
            if (authenticated(request) && !request.headers("Name").equals("Leo Forney")) {
                deleteUser(request.headers("Name"), response);
            } else {
                response.status(403);
            }
            return response.body();
        });
    }

    public static boolean authenticated(Request request) throws Exception {
        Statement statement = Runner.connection.createStatement();

        ResultSet rs = statement.executeQuery("select * from Users WHERE Token = \"" + request.headers("Token") + "\"");

        List<String> names = new ArrayList<>();
        while (rs.next()) {
            // read the result set
            names.add(rs.getString("Name"));
        }

        rs.close();

        statement.close();

        if (names.size() == 1) {
            return true;
        } else {
            return false;
        }

    }

    void createUser(User user, Response response) {
        String executionStatement = "INSERT INTO Users (Name, Token) VALUES (\'" + user.name +"\', '" + user.token + "')";

        PreparedStatement statement = null;

        try {
            statement = connection.prepareStatement(executionStatement);
            statement.execute();
            response.body("Created user successfully");
        } catch (SQLException e) {
            response.body("Failed to create user");
        }

        try {
            connection.commit();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    void deleteUser(String name, Response response) {
        String executionStatement = "DELETE FROM Users WHERE Name = \"" + name + "\"";

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(executionStatement);
            statement.execute();
            response.body("Deleted user successfully");
        } catch (SQLException e) {
            response.body("Failed to delete user");
        }

        try {
            connection.commit();
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
