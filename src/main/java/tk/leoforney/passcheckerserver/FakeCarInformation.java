package tk.leoforney.passcheckerserver;

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

    }

    private String[] makes = {"Honda", "Toyota", "Scion", "Mercedes", "Chevy", "Ford", "Hyundai", "Alpha-Romeo", "Volvo", "Volkswagen", "Mazda", "Jaguar", "BMW", "Lexus", "Acura", "Infinity", "Subaru", "Mazerati", "Tesla"};
    private String[] models = {"Focus", "Fit", "Highlander", "Elantra", "Malibu", "Cruze", "Rav4", "Accord", "Camry", "Odyssey", "Sienna", "Jetta", "Golf", "Escape", "CR-V", "HR-V", "6", "F150", "Trailblazer", "Yukon", "Outback", "Crosstrek", "RC350"};

    public String getMake() {
        return makes[new Random().nextInt(makes.length)];
    }

    public String getModel() {
        return models[new Random().nextInt(models.length)];
    }

    public String getYear() {
        int startYear = 2000;
        int amplitude = 22;
        return String.valueOf(new Random().nextInt(amplitude) * (new Random().nextBoolean() ? -1 : 1));
    }
}