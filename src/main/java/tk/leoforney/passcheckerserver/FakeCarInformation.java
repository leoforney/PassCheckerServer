package tk.leoforney.passcheckerserver;

import com.github.javafaker.Faker;
import org.apache.commons.lang3.text.WordUtils;
import org.jsoup.helper.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class FakeCarInformation {

    private static FakeCarInformation instance = null;

    public static FakeCarInformation getInstance() {
        if (instance == null) {
            instance = new FakeCarInformation();
        }
        return instance;
    }

    private FakeCarInformation() {
        random = new Random();
        faker = new Faker();
    }

    int count = 0;
    HashMap<String, Boolean> nameMap;
    Faker faker;
    Random random;

    private String[] makes = {"Honda", "Toyota", "Scion", "Mercedes", "Chevy", "Ford", "Hyundai", "Alpha-Romeo", "Volvo", "Volkswagen", "Mazda", "Jaguar", "BMW", "Lexus", "Acura", "Infinity", "Subaru", "Mazerati", "Tesla"};
    private String[] models = {"Focus", "Fit", "Highlander", "Elantra", "Malibu", "Cruze", "Rav4", "Accord", "Camry", "Odyssey", "Sienna", "Jetta", "Golf", "Escape", "CR-V", "HR-V", "6", "F150", "Trailblazer", "Yukon", "Outback", "Crosstrek", "RC350"};

    public String getMake() {
        return makes[random.nextInt(makes.length)];
    }

    public String getModel() {
        return models[random.nextInt(models.length)];
    }

    public String getYear() {
        int startYear = 2000;
        int amplitude = 35;
        boolean negative = random.nextBoolean();
        int randomAmplitude = random.nextInt(amplitude) * (negative ? -1 : 1);
        return String.valueOf(startYear + randomAmplitude);
    }

    public int getID() {
        int totalID = 140000 + count;
        count++;
        return totalID;
    }

    public String getName() {
        if (nameMap == null) {
            nameMap = new HashMap<>();
        }
        String generatedName = faker.name().firstName() + " " + faker.name().lastName();
        if (nameMap.containsKey(generatedName)) {
            generatedName = generatedName + "ialia";
        }
        nameMap.put(generatedName, true);

        return generatedName;
    }

    public Student newFakeStudent() {
        return new Student(getName(), getID());
    }

    public Car newFakeCar(String plate, int id) {
        Car car = new Car();
        car.setColor(WordUtils.capitalize(faker.color().name()));
        car.setId(id);
        car.setYear(Integer.parseInt(getYear()));
        car.setPlateNumber(plate);
        car.setMake(getMake());
        car.setModel(getModel());
        return car;
    }

}