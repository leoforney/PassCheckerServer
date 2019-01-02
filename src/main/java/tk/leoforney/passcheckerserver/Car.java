package tk.leoforney.passcheckerserver;

import java.sql.ResultSet;

/**
 * Created by Leo on 7/27/2018.
 */
public class Car {
    public String getPlateNumber() {
        return plateNumber;
    }

    public void setPlateNumber(String plateNumber) {
        this.plateNumber = plateNumber;
    }

    public String getMake() {
        return make;
    }

    public void setMake(String make) {
        this.make = make;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String plateNumber, make, model, color;
    public int year, id;

    public Car() {

    }

    public Car(ResultSet rs) {
        try {
            this.plateNumber = rs.getString("plateNumber");
            this.color = rs.getString("color");
            this.make = rs.getString("make");
            this.model = rs.getString("model");
            this.id = rs.getInt("studentId");
            this.year = rs.getInt("year");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Car(String plateNumber, String color, String make, String model, int year, int id) {
        this.plateNumber = plateNumber;
        this.color = color;
        this.make = make;
        this.model = model;
        this.year = year;
        this.id = id;
    }

    @Override
    public String toString() {
        return plateNumber + " : " + make + " : " + model + " : " + color + " : " + year + " : " + id;
    }

}
