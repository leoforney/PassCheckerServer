package tk.leoforney.passcheckerserver;

public class DatabaseResponse {
    public Student student;
    public Car car;

    public DatabaseResponse(Student student, Car car) {
        this.student = student;
        this.car = car;
    }
}
