package tk.leoforney.passcheckerserver;

public abstract class Person {
    public String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFirstName() {
        return(name.split("\\s+")[0]);
    }

    public String getLastName() {
        return(name.split("\\s+")[1]);
    }


}
