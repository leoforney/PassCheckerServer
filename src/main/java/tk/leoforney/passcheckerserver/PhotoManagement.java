package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.openalpr.jni.Alpr;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static tk.leoforney.passcheckerserver.Main.wd;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * Created by Leo on 7/27/2018.
 */
@RestController
public class PhotoManagement {

    Gson gson;
    Alpr alpr;
    PassManagement passManagement;
    public static File uploadDir;
    private final static Logger logger = Logger.getLogger(PhotoManagement.class.getName());

    public PhotoManagement(PassManagement passManagement) {
        this.passManagement = passManagement;

        uploadDir = new File(wd + File.separator + "upload");

        gson = new Gson();

        String os = System.getProperty("os.name");
        String username = System.getProperty("user.name");

        if (os.toLowerCase().contains("nix") || os.toLowerCase().contains("nux")) {
            alpr = new Alpr("us", "/etc/openalpr/openalpr.conf", "/usr/share/openalpr/runtime_data");
        } else if (os.toLowerCase().contains("win")) {
            alpr = new Alpr("us", "C:/Users/" + username + "/openalpr/openalpr.conf", "C:/Users/" + username + "/openalpr/runtime_data/");
        } else {
            alpr = new Alpr("us", "/usr/local/share/openalpr/openalpr.conf", "/usr/local/share/openlapr/runtime_data");
        }
        alpr.setTopN(3);
        alpr.setDefaultRegion("il");
    }

    private boolean latestInUse = false;

    @RequestMapping(value = "/plateNumber", method = RequestMethod.GET)
    public String plateNumberGet() {
        return "<form method='post' enctype='multipart/form-data'>" // note the enctype
                + "    <input type='file' name='image' accept='.jpg'>" // make sure to call getPart using the same "name" in the post
                + "    <input type'text' name='Token'>"
                + "    <button>Upload picture</button>"
                + "</form>";
    }

    @RequestMapping(value = "/checkInDatabase", method = RequestMethod.GET)
    public String checkInDatabase() {
        return "<form method='post' enctype='multipart/form-data'>" // note the enctype
                + "    <input type='file' name='image' accept='.jpg'>" // make sure to call getPart using the same "name" in the post
                + "    <input type'text' name='Token'>"
                + "    <button>Upload picture</button>"
                + "</form>";
    }

    @RequestMapping(value = "/getStudent", method = RequestMethod.POST)
    public String getStudent(@RequestHeader(value = "Token") String token,
                             @RequestPart(value = "image") MultipartFile file,
                             @RequestHeader(value = "Sender", required = false) String sender) {
        String response = "";
        if (authenticated(token)) {
            List<String> plateNumberList = null;
            try {
                plateNumberList = PhotoQueue.getInstance().request(file, sender);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String plateNumber = plateNumberList.get(0);
            String student = passManagement.findStudentNameByPlateNumber(plateNumber);
            System.out.println("Student: " + student);
            response = gson.toJson(passManagement.findStudentByPlateNumber(plateNumber));
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = "/getStudentName", method = RequestMethod.POST)
    public String getStudentName(@RequestHeader(value = "Token") String token,
                                 @RequestPart(value = "image") MultipartFile file,
                                 @RequestHeader(value = "Sender", required = false) String sender) {
        String response = "";
        if (authenticated(token)) {
            List<String> plateNumberList = null;
            try {
                plateNumberList = PhotoQueue.getInstance().request(file, sender);
            } catch (Exception e) {
                e.printStackTrace();
            }
            String plateNumber = plateNumberList.get(0);
            System.out.println(plateNumber);
            if (plateNumber.equals("No Plates Detected".toLowerCase())) {
                response = plateNumber;
            }
            response = passManagement.findStudentNameByPlateNumber(plateNumber);
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = "/plateNumber", method = RequestMethod.POST)
    public String plateNumber(@RequestHeader(value = "Token", required = true) String token,
                              @RequestParam(value = "image") MultipartFile file,
                              @RequestHeader(value = "Sender", required = false) String sender) {
        String response = "";
        if (authenticated(token)) {
            try {
                List<String> plateNumbers = PhotoQueue.getInstance().request(file, sender);
                for (String str : plateNumbers) {
                    response = response + str + "\n";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            response = "403";
        }
        return response;
    }

    @RequestMapping(value = "/checkInDatabase", method = RequestMethod.POST)
    public String checkInDatabase(@RequestHeader(value = "Token", required = false) String token,
                                  @RequestPart(value = "image") MultipartFile file,
                                  @RequestHeader(value = "Sender", required = false) String sender) {
        String response = "";
        ScanLogger scanLog = ScanLogger.getInstance();
        if (/*authenticated(token)*/true) {
            List<String> plateNumberList = new ArrayList<>();
            try {
                plateNumberList = PhotoQueue.getInstance().request(file, sender);
            } catch (Exception e) {
                e.printStackTrace();
                plateNumberList.add("noplatesdetected");
                plateNumberList.add(String.valueOf(System.currentTimeMillis()));
            }
            String plateNumber = plateNumberList.get(0);
            DatabaseResponse databaseResponse = new DatabaseResponse();
            databaseResponse.setTimestamp(plateNumberList.get(1));
            System.out.println(databaseResponse.getTimestamp());
            if (!plateNumber.toLowerCase().replace(" ", "").contains("noplatesdetected")) {
                Student responseStudent = passManagement.checkInStudent(plateNumber);
                if (responseStudent.name != null) {
                    Car selectedCar = null;
                    for (Car iteratedCar : responseStudent.cars) {
                        if (iteratedCar.plateNumber.toLowerCase().replace(" ", "")
                                .equals(plateNumber.toLowerCase().replace(" ", ""))) {
                            selectedCar = iteratedCar;
                        }
                    }
                    databaseResponse.setStudent(responseStudent);
                    if (selectedCar != null) {
                        databaseResponse.setCar(selectedCar);
                        if (responseStudent.getPassType().isPassValid()) {
                            databaseResponse.setType(DatabaseResponse.Type.OK);
                            scanLog.log(plateNumber + " - " + responseStudent.name);
                        } else {
                            databaseResponse.setType(DatabaseResponse.Type.PASSINVALID);
                            scanLog.log(plateNumber + " - " + responseStudent.name + "[INVALID]");
                        }
                    } else {
                        databaseResponse.setType(DatabaseResponse.Type.STUDENTONLY);
                    }
                } else {
                    databaseResponse.setPlateNumber(plateNumber);
                    databaseResponse.setType(DatabaseResponse.Type.PLATEONLY);
                    scanLog.log(plateNumber);
                }
            } else {
                databaseResponse.setType(DatabaseResponse.Type.NOPLATES);
            }
            logger.log(Level.INFO, databaseResponse.getType().toString());
            response = gson.toJson(databaseResponse);
        } else {
            response = "403";
        }
        return response;
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
