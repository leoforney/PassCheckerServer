package tk.leoforney.passcheckerserver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PassType {

    public List<String> getDayPasses() {
        return dayPasses;
    }

    public void setDayPasses(List<String> dayPasses) {
        this.dayPasses = dayPasses;
    }

    public void addDayPass(Date date) {
        addDayPass(SemesterUtil.getInstance().getFullFormat().format(date));
    }

    public void addDayPass(String day) {
        if (dayPasses == null) {
            dayPasses = new ArrayList<>();
        }
        if (type != null && !type.equals(Type.FULLYEAR)) {
            dayPasses.add(day);
        }
    }

    // TODO: Implement 5 day pass limit per semester

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    private List<String> dayPasses;
    private Type type;

    public boolean isPassValid() {
        if (type.equals(Type.FULLYEAR)) {
            return true;
        }

        SemesterUtil util = SemesterUtil.getInstance();
        SemesterUtil.Semester currentSemester = util.getCurrentSemester();

        if (type.equals(Type.FIRSTSEMESTER)) {
            if (currentSemester.equals(SemesterUtil.Semester.FIRST)) {
                return true;
            }
        }

        if (type.equals(Type.SECONDSEMESTER)) {
            if (currentSemester.equals(SemesterUtil.Semester.SECOND)) {
                return true;
            }
        }

        // Check for current dayPass
        Date currentDate = new Date();
        for (String day: dayPasses) {
            if (util.getFullFormat().format(currentDate).equals(day)) {
                return true;
            }
        }

        return false;
    }

    enum Type {
        FULLYEAR,
        FIRSTSEMESTER,
        SECONDSEMESTER,
        NONE
    }

}
