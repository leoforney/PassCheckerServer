package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import spark.Request;
import spark.Response;

import java.net.URI;
import java.sql.*;
import java.util.ArrayList;
import java.util.Base64;
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

    private static UserManagement instance = null;

    public static UserManagement getInstance() {
        if (instance == null) {
            instance = new UserManagement(Runner.connection);
        }
        return instance;
    }

    private UserManagement(Connection connection) {
        gson = new Gson();
        this.connection = connection;
        //System.out.println(gson.toJson(new User("Leo Forney", "Zm9ybmU2OTVAd3Rocy5uZXQ6ZGFyeWxlbzE=")));

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

        get(PATH + "/validateuser/json", (request, response) -> {
            Statement statement = connection.createStatement();

            String token = request.headers("Token");

            System.out.println("Trying to find " + token);

            ResultSet rs = statement.executeQuery("SELECT * FROM Users");

            List<User> userList = new ArrayList<>();
            User correctUser = null;
            while (rs.next()) {
                User newUser = new User(rs.getString("Name"), rs.getString("Token"));
                System.out.println(newUser.token + " : " + newUser.name);
                if (newUser.isValid() && newUser.token.equals(token)) {
                    if (correctUser == null) {
                        correctUser = newUser;
                    }
                }
            }

            userList.clear();
            if (correctUser != null) {
                userList.add(correctUser);
            }

            if (userList.size() == 1) {
                response.body(gson.toJson(userList.get(0)));
            }

            rs.close();
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
            if (authenticated(request) /*&& !request.headers("Name").equals("Leo Forney")*/) {
                deleteUser(request.headers("Name"), response);
            } else {
                response.status(403);
            }
            return response.body();
        });
    }

    public static boolean authenticated(Request request) {
        return authenticated(request.headers("Token") + "\"");
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

    void createUser(User user, Response response) {
        String executionStatement = "INSERT INTO Users (Name, Token) VALUES (\'" + user.name + "\', '" + user.token + "')";

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

    User userFromToken(String token) {
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

    List<User> usersList() {
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

}
