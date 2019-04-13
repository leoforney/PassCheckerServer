package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import org.apache.commons.codec.binary.Base64;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private Connection sqlConnection;
    
    private final static String PREFIX = "/pass";
    private final static Logger logger = Logger.getLogger(PassManagement.class.getName());

    List<Student> students;
    public static final Base64 base64 = new Base64();

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
        sqlConnection = SpringUtils.connection;
        PassType type = new PassType();
        type.setType(PassType.Type.NONE);
        type.addDayPass("03192019");
        System.out.println(gson.toJson(type));
    }

    public String createCar(@NonNull String carJson) {
        return createStudent(gson.toJson(carJson));
    }

    public String createCar(@NonNull Car record) {
        String response = "";
        Statement statement = null;
        try {
            statement = sqlConnection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            String sqlString = "INSERT INTO Cars (plateNumber, make, model, year, studentId, color) VALUES (" + creationFromCar(record) + ");";
            System.out.println(sqlString);
            boolean result = statement.execute(sqlString);
            System.out.println(result);
            response = "Created car successfully";
        } catch (SQLException e) {
            response = "Failed to create car";
        }

        try {
            sqlConnection.commit();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String deleteCar(@NonNull Car car) {
        return deleteCar(car.plateNumber);
    }

    public String deleteCar(@NonNull String plateNumber) {
        String executionStatement = "DELETE FROM Cars WHERE plateNumber = \'" + plateNumber + "\'";
        boolean statementResult = executeStatement(executionStatement);
        if (statementResult) {
            return "Deleted car";
        }
        return "Failed to delete car";
    }

    private boolean executeStatement(String executionStatement) {
        boolean response = false;
        PreparedStatement statement = null;

        try {
            statement = sqlConnection.prepareStatement(executionStatement);
            response = statement.execute();
        } catch (SQLException e) {
            response = false;
        }

        try {
            sqlConnection.commit();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    public String deleteStudent(Student student) {
        return deleteStudent(student.name, student.id);
    }

    public String deleteStudent(@Nullable String name, @Nullable Integer id) {
        String executionStatement = null;
        if (name != null) {
            executionStatement = "DELETE FROM Students WHERE name = \'" + name + "\'";
        }
        if (id != null) {
            executionStatement = "DELETE FROM Students WHERE id = " + id;
        }

        boolean statementResult = executeStatement(executionStatement);
        if (statementResult) {
            return "Deleted student";
        }
        return "Student doesn't exist";
    }

    public Student updateStudent(Student student) {
        try {
            Statement statement = sqlConnection.createStatement();
            String base64Encoded = new String(base64.encode(gson.toJson(student.getPassType()).getBytes()));
            String statementString = "UPDATE Students SET name = \"" + student.getName() + "\"," +
                    " id = \"" + student.id +
                    "\", passType = \"" + base64Encoded +
                    "\" WHERE id = " + student.id + " OR name = \"" + student.getName() + "\"";
            logger.log(Level.INFO, statementString);
            int result = statement.executeUpdate(statementString);
            sqlConnection.commit();
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return student;
    }

    String createStudent(@NonNull String studentJson) {
        return createStudent(gson.fromJson(studentJson, Student.class));
    }

    public String createStudent(Student student) {
        String executionStatement = "INSERT INTO Students (name, id) VALUES (\'" + student.name + "\', '" + student.id + "')";

        String response = "";
        PreparedStatement statement = null;

        try {
            statement = sqlConnection.prepareStatement(executionStatement);
            statement.execute();
            response = "Created student successfully";
        } catch (SQLException e) {
            response = student.name + " already exists in database";
        }

        try {
            sqlConnection.commit();
            if (statement != null) {
                statement.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return response;
    }

    public List<Student> getStudentList() {
        try {
            Statement statement = SpringUtils.connection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Students");

            students = new ArrayList<>();

            while (rs.next()) {
                Student student = new Student(rs);
                students.add(student);
            }

            rs.close();

            statement.close();

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
            Statement statement = SpringUtils.connection.createStatement();
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

            statement.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return cars;
    }

    public void updateCarFromPlateNumber(String plateNumber, Car after) {
        try {
            Statement statement = sqlConnection.createStatement();
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
                if (car.plateNumber.toLowerCase().replace(" ", "").equals(plateNumber.toLowerCase())) {
                    return student;
                }
            }
        }

        return new Student();
    }

    public Student getStudent(String name) {
        Student student = new Student();
        try {
            Statement statement = sqlConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Students WHERE name = \"" + name + "\"");

            student = new Student(rs);

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return student;
    }

    public Student getStudent(int id) {
        Student student = new Student();
        try {
            Statement statement = sqlConnection.createStatement();
            ResultSet rs = statement.executeQuery("SELECT * FROM Students WHERE id = \"" + id + "\"");
            student = new Student(rs);

            rs.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return student;
    }

    public Car findCarByPlateNumber(String plateNumber) {
        Car foundCar = new Car();
        try {
            Statement statement = sqlConnection.createStatement();
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
                            if (!s.contains("_id") && !s.contains("plateNumber")) {
                                try {
                                    dateList.add(dateFormat.parse(s));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
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

    @RequestMapping(value = PREFIX + "/student/{id}", method = RequestMethod.DELETE)
    public String deleteStudentRestFromId(@RequestHeader(value = "Token") String token,
                                          @PathVariable Integer id) {
        if (authenticated(token)) {
            return deleteStudent(null, Integer.valueOf(id));
        }
        return "403";
    }

    @RequestMapping(value = PREFIX + "/student", method = RequestMethod.DELETE)
    public String deleteStudentRest(@RequestHeader(value = "Token") String token,
                                    @RequestBody String body) {
        if (authenticated(token)) {
            return deleteStudent(gson.fromJson(body, Student.class));
        }
        return "403";
    }


    @RequestMapping(value = PREFIX + "/student", method = RequestMethod.POST)
    public String createStudentRest(@RequestHeader(value = "Token") String token,
                                    @RequestBody String body) {
        if (authenticated(token)) {
            return createStudent(body);
        }
        return "403";
    }

    @RequestMapping(value = PREFIX + "/student/update", method = RequestMethod.POST)
    public String updateStudentRest(@RequestHeader(value = "Token") String token,
                                    @RequestBody String body) {
        if (authenticated(token)) {
            Student updatedStudent = gson.fromJson(body, Student.class);
            return gson.toJson(updateStudent(updatedStudent));
        }
        return "403";
    }

    @RequestMapping(value = PREFIX + "/car/**", method = RequestMethod.DELETE)
    public String deleteCar(@RequestHeader(value = "Token") String token,
                            @PathVariable String licensePlate) {
        String response;
        if (authenticated(token)) {
            Statement statement = null;
            try {
                statement = sqlConnection.createStatement();
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
            response = createCar(recordStringJson);
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
