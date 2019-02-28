package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.web.bind.annotation.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.set;
import static tk.leoforney.passcheckerserver.Runner.checkDatabase;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * Created by Leo on 5/5/2018.
 */
@RestController
public class PassManagement {

    Gson gson;
    Connection connection;
    private final static String PREFIX = "/pass";

    List<Student> students;

    private static PassManagement instance = null;
    DateFormat dateFormat = new SimpleDateFormat("MMddyyyy");

    public static PassManagement getInstance() {
        if (instance == null) {
            instance = new PassManagement();
        }
        return instance;
    }

    private PassManagement() {
        gson = new Gson();
        this.connection = Runner.connection;

    }

    @RequestMapping("/greeting")
    public String greeting(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello from REST!";
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
                while (cursor.hasNext()) {
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

    @RequestMapping(value = PREFIX + "/car/**", method = RequestMethod.DELETE)
    public String deleteCar(@RequestHeader(value = "Token") String token,
                            @PathVariable String licensePlate) {
        String response;
        if (authenticated(token)) {
            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                statement.executeUpdate("DELETE FROM Cars WHERE plateNumber=" + licensePlate);
                response = "Successfully deleted car";
            } catch (SQLException e) {
                response = "Failed to delete car";
            }
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/car/**", method = RequestMethod.GET)
    public String getCar(@RequestHeader(value = "Token") String token,
                         @PathVariable String studentIdStr) {
        String response = "";
        if (authenticated(token)) {
            int studentId = Integer.valueOf(studentIdStr);
            List<Student> students = getStudentList();
            for (Student student : students) {
                if (student.id == studentId) {
                    response = gson.toJson(student.cars);
                }
            }
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/car", method = RequestMethod.POST)
    public String createCar(@RequestHeader(value = "Token") String token,
                            @RequestBody String recordStringJson) {
        String response = "";
        if (authenticated(token)) {
            Car record = gson.fromJson(recordStringJson, Car.class);

            Statement statement = null;
            try {
                statement = connection.createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                String sqlString = "INSERT INTO Cars VALUES (" + creationFromCar(record) + ")";
                System.out.println(sqlString);
                statement.executeUpdate(sqlString);
                response = "Created car successfully";
            } catch (SQLException e) {
                response = "Failed to create car";
            }
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/listcars/json", method = RequestMethod.GET)
    public String getCar(@RequestHeader(value = "Token") String token) {
        System.out.println(token);
        String response = "";
        if (authenticated(token)) {
            List<Car> carList = getCarList();
            response = gson.toJson(carList);
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/student/all/json", method = RequestMethod.GET)
    public String getAllStudents(@RequestHeader(value = "Token") String token) {
        String response = "";
        if (authenticated(token)) {
            List<Student> students = getStudentList();

            response = gson.toJson(students);
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/listcars/text", method = RequestMethod.GET)
    public String getAllCarsString(@RequestHeader(value = "Token") String token) {
        String response = "";
        System.out.println(token);
        if (authenticated(token)) {
            List<Car> carList = getCarList();

            StringBuilder sb = new StringBuilder();

            for (Car car : carList) {
                System.out.println(car.toString());
                sb.append(car.toString()).append("\n");
            }

            String responseString = sb.toString();
            System.out.println(responseString);

            response = responseString;
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = PREFIX + "/find/**", method = RequestMethod.GET)
    public String find(@RequestHeader(value = "Token") String token,
                       @PathVariable String plateNumber) {
        String response = "";
        if (authenticated(token)) {
            response = findStudentJsonByPlateNumber(plateNumber);
        } else {
            response = "403";
        }
        return response;
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
