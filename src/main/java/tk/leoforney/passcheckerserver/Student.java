package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 7/27/2018.
 */
public class Student extends Person {

    private static final Gson gson = new Gson();

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }

    public PassType getPassType() {
        if (passType == null) {
            String decoded = new String(PassManagement.base64.decode("eyJ0eXBlIjoiRlVMTFlFQVIifQ=="));
            this.passType = gson.fromJson(decoded, PassType.class);
        }
        return passType;
    }

    public void setPassType(PassType passType) {
        this.passType = passType;
    }

    public int id;
    public List<Car> cars;
    public PassType passType;

    public Student(ResultSet rs) {
        try {
            this.name = rs.getString("name");
            this.id = rs.getInt("id");
            String passTypeBase64 = rs.getString("passType");
            if (passTypeBase64 == null) {
                passTypeBase64 = "eyJ0eXBlIjoiRlVMTFlFQVIifQ==";
            }
            System.out.println(passTypeBase64);
            String decoded = new String(PassManagement.base64.decode(passTypeBase64));
            this.passType = gson.fromJson(decoded, PassType.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (cars == null) cars = new ArrayList<>();
    }

    public Student() {
        if (cars == null) cars = new ArrayList<>();
    }

    public Student(String name, int id) {
        this.name = name;
        this.id = id;
        if (cars == null) cars = new ArrayList<>();
    }

    public Student(String name, int id, PassType type) {
        this.name = name;
        this.id = id;
        passType = type;
        if (cars == null) cars = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + " : " + id;
    }
}
