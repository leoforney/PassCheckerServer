package tk.leoforney.passcheckerserver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Leo on 7/27/2018.
 */
public class Student {
    public String name;
    public int id;
    public List<Car> cars;

    public Student() {
        if (cars == null) cars = new ArrayList<>();
    }

    public Student(String name, int id) {
        this.name = name;
        this.id = id;
        if (cars == null) cars = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name + " : " + id;
    }
}
