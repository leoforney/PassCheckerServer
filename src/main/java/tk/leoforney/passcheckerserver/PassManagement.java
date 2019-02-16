package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static spark.Spark.*;
import static tk.leoforney.passcheckerserver.Runner.checkDatabase;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * Created by Leo on 5/5/2018.
 */
public class PassManagement {

    Gson gson;
    Connection connection;
    private final static String PREFIX = "/pass";

    List<Student> students;

    private static PassManagement instance = null;
    DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");

    public static PassManagement getInstance() {
        if (instance == null) {
            instance = new PassManagement(Runner.connection);
        }
        return instance;
    }

    private PassManagement(Connection connection) {
        gson = new Gson();
        this.connection = connection;

        // Delete the car in the database
        delete(PREFIX + "/car/*", (request, response) -> {
            String licensePlate = request.splat()[0];
            if (authenticated(request)) {
                Statement statement = connection.createStatement();
                try {
                    statement.executeUpdate("DELETE FROM Cars WHERE plateNumber=" + licensePlate);
                    response.body("Successfully deleted car");
                } catch (SQLException e) {
                    response.body("Failed to delete car");
                }
            } else {
                response.status(403);
            }
            return response.body();
        });

        // Get car of student
        get(PREFIX + "/car/*", (request, response) -> {
            int studentId = Integer.valueOf(request.splat()[0]);
            if (authenticated(request)) {
                List<Student> students = getStudentList();
                for (Student student : students) {
                    if (student.id == studentId) {
                        response.body(gson.toJson(student.cars));
                    }
                }
            } else {
                response.status(403);
            }
            return response.body();
        });

        // Create a car
        post(PREFIX + "/car/create", (request, response) -> {
            if (authenticated(request)) {
                String recordStringJson = request.body();
                Car record = gson.fromJson(recordStringJson, Car.class);

                Statement statement = connection.createStatement();
                try {
                    String sqlString = "INSERT INTO Cars VALUES (" + creationFromCar(record) + ")";
                    System.out.println(sqlString);
                    statement.executeUpdate(sqlString);
                    response.body("Created car successfully");
                } catch (SQLException e) {
                    response.body("Failed to create car");
                }
            } else {
                response.status(403);
            }

            return response.body();
        });

        // List all cars in json
        get(PREFIX + "/listcars/json", (request, response) -> {
            if (authenticated(request)) {
                List<Car> carList = getCarList();
                response.body(gson.toJson(carList));
            } else {
                response.status(403);
            }
            return response.body();
        });

        get(PREFIX + "/student/all/json", (request, response) -> {
            if (authenticated(request)) {
                List<Student> students = getStudentList();

                String studentsString = gson.toJson(students);

                return studentsString;
            } else {
                response.status(403);
            }
            return "An error has occurred";
        });

        get(PREFIX + "/listcars/text", (request, response) -> {
            if (authenticated(request)) {
                List<Car> carList = getCarList();

                StringBuilder sb = new StringBuilder();

                for (Car car : carList) {
                    System.out.println(car.toString());
                    sb.append(car.toString()).append("\n");
                }

                String responseString = sb.toString();
                System.out.println(responseString);

                response.body(responseString);
            } else {
                response.status(403);
            }
            return response.body();
        });

        get(PREFIX + "/find/*", (request, response) -> {
            String plateNumber = request.splat()[0];
            if (authenticated(request)) {
                response.body(findStudentJsonByPlateNumber(plateNumber));
            } else {
                response.status(403);
            }
            return response.body();
        });

    }

    public List<Student> getStudentList() {
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Students");

            students = new ArrayList<>();

            while (rs.next()) {
                Student student = new Student(rs.getString("name"), rs.getInt("id"));
                students.add(student);
            }

            rs.close();

            List<Car> carList = getCarList();

            for (Car car : carList) {

                for (Student student : students) {
                    if (car.id == student.id) {
                        student.cars.add(car);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return students;

    }

    public List<Car> getCarList() {
        List<Car> cars = new ArrayList<>();

        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Cars");

            while (rs.next()) {
                Car car = new Car(
                        rs.getString("plateNumber"),
                        rs.getString("color"),
                        rs.getString("make"),
                        rs.getString("model"),
                        rs.getInt("year"),
                        rs.getInt("studentId")
                );

                cars.add(car);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cars;
    }

    public void updateCarFromPlateNumber(String plateNumber, Car after) {
        try {
            Statement statement = connection.createStatement();
            int result = statement.executeUpdate("UPDATE Cars SET plateNumber = \"" + after.plateNumber + "\"," +
                    " make = \"" + after.make +
                    "\", model = \"" + after.model +
                    "\", year = \"" + after.year +
                    "\", color = \"" + after.color +
                    "\", studentId = \"" + after.id +
                    "\" WHERE plateNumber = \"" + plateNumber + "\"");
        } catch (Exception e) {

        }
    }

    public String findStudentJsonByPlateNumber(String plateNumber) {
        List<Student> studentList = getStudentList();

        assert studentList != null;
        for (Student student : studentList) {
            for (Car car : student.cars) {
                if (car.plateNumber.toLowerCase().equals(plateNumber.toLowerCase())) {
                    return gson.toJson(student);
                }
            }
        }

        return "";
    }

    public Student findStudentByPlateNumber(String plateNumber) {
        List<Student> studentList = getStudentList();

        assert studentList != null;
        for (Student student : studentList) {
            for (Car car : student.cars) {
                if (car.plateNumber.toLowerCase().equals(plateNumber.toLowerCase())) {
                    return student;
                }
            }
        }

        return new Student();
    }

    public Car findCarByPlateNumber(String plateNumber) {
        Car foundCar = new Car();
        try {
            Statement statement = connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Cars WHERE plateNumber = \"" + plateNumber + "\"");

            foundCar = new Car(rs);

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return foundCar;
    }

    public String findStudentNameByPlateNumber(String plateNumber) {
        List<Student> studentList = getStudentList();

        assert studentList != null;
        for (Student student : studentList) {
            for (Car car : student.cars) {
                if (car.plateNumber.toLowerCase().equals(plateNumber.toLowerCase())) {
                    return student.name;
                }
            }
        }

        return "";
    }

    public Student checkInStudent(String plateNumber) {
        System.out.println(plateNumber);
        Student student = findStudentByPlateNumber(plateNumber);
        if (student.name != null) {
            MongoCollection<Document> idCollection = checkDatabase.getCollection(String.valueOf(student.id));
            long amount = idCollection.countDocuments();
            System.out.println("Amount of documents: " + amount);
            if (amount == 0) {
                checkDatabase.createCollection(String.valueOf(student.id));
                idCollection = checkDatabase.getCollection(String.valueOf(student.id));
            }

            FindIterable<Document> fi = idCollection.find();
            MongoCursor<Document> cursor = fi.iterator();
            int size = 0;
            try {
                while(cursor.hasNext()) {
                    Document document = cursor.next();
                    int datesChecked = document.size() - 2;
                    if (datesChecked > 4) {
                        List<Date> dateList = new ArrayList<>();
                        for (String s : document.keySet()) {
                            try {
                                dateList.add(dateFormat.parse(s));
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        }
                        Collections.sort(dateList);
                        //TODO Remove index 0 from list from MongoDB
                    }
                    size++;
                    System.out.println(document.toJson());
                }
            } finally {
                cursor.close();
            }

            if (size == 0) {
                Document document = new Document("plateNumber", plateNumber.toLowerCase()).append("_id", new ObjectId());
                idCollection.insertOne(document);
            }

            idCollection.updateOne(eq("plateNumber", plateNumber), set(dateFormat.format(new Date()), "checked"));

        }

        return student;
    }

    private String creationFromCar(Car car) {
        return "\"" + car.plateNumber + "\", \"" +
                car.make + "\", \"" +
                car.model + "\", " +
                car.year + ", " +
                car.id + ", \"" +
                car.color + "\"";
    }

}
