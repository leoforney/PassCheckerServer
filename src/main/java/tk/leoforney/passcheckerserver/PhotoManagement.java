package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import javaxt.io.Image;
import org.apache.commons.io.IOUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

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

    public PhotoManagement(PassManagement passManagement) {
        this.passManagement = passManagement;

        uploadDir = new File(wd + File.separator + "upload");

        gson = new Gson();

        String os = System.getProperty("os.name");
        String username = System.getProperty("user.name");
        System.out.println(os + " - " + username);

        if (os.toLowerCase().contains("nix") || os.toLowerCase().contains("nux")) {
            alpr = new Alpr("us", "/etc/openalpr/openalpr.conf", "/usr/share/openalpr/runtime_data");
        } else {
            alpr = new Alpr("us", "C:/Users/" + username + "/openalpr/openalpr.conf", "C:/Users/" + username + "/openalpr/runtime_data/");
        }
        alpr.setTopN(3);
        alpr.setDefaultRegion("il");
    }

    public List<String> getPlateNumberFromRequest(MultipartFile file, String sender) throws Exception {

        List<String> responseList = new ArrayList<>(2);

        String timestamp = String.valueOf(System.currentTimeMillis());
        File photoFile = new File(uploadDir.getAbsolutePath() + File.separator + timestamp
                + ".jpg");
        File latestPhoto = new File(uploadDir.getAbsolutePath() + File.separator + "latest.jpg");
        if (!latestPhoto.exists()) latestPhoto.createNewFile();

        if (photoFile.exists()) {
            photoFile.delete();
        }

        photoFile.createNewFile();

        //request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

        if (sender != null) {
            //Part part = request.raw().getPart("image");
            if (file.getContentType().toLowerCase().equals("image/yuv_420_88")) {

            } else if (file.getContentType().toLowerCase().equals("image/jpeg")) {
                try (InputStream input = file.getInputStream()) { // getPart needs to use same "name" as input field in form
                    byte[] resultByteData = IOUtils.toByteArray(input);
                    Image image = new Image(resultByteData);
                    image.rotateClockwise();
                    Files.write(photoFile.toPath(), image.getByteArray(), StandardOpenOption.WRITE);
                    Files.copy(photoFile.toPath(), latestPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } else {
            try (InputStream input = file.getInputStream()) { // getPart needs to use same "name" as input field in form
                if (!photoFile.exists()) photoFile.createNewFile();
                Files.copy(input, photoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(photoFile.toPath(), latestPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
        }


        String returnValue = "";
        if (alpr.isLoaded()) {
            AlprResults results = alpr.recognize(photoFile.toString());
            System.out.format("  %-15s%-8s\n", "Plate Number", "Confidence");
            System.out.println(results.getPlates().size());
            if (results.getPlates().size() == 0) {
                returnValue = "No Plates Detected";
            }
            for (AlprPlateResult result : results.getPlates()) {
                for (AlprPlate plate : result.getTopNPlates()) {
                    if (plate.isMatchesTemplate())
                        System.out.print("  * ");
                    else
                        System.out.print("  - ");
                    String plateChars = plate.getCharacters();
                    if (returnValue.equals("")) returnValue = plateChars;
                    System.out.format("%-15s%-8f\n", plateChars, plate.getOverallConfidence());
                    System.out.println("Total time: " + result.getProcessingTimeMs());
                }
                returnValue = result.getTopNPlates().get(0).getCharacters();
            }
        }
        if (returnValue.equals("No Plates Detected")) {
            photoFile.delete();
        } else {

        }

        deleteOldestImage();

        responseList.add(returnValue.toLowerCase()); // 0 - Plate number (lowercase)
        responseList.add(timestamp); // 1 - timestamp as string

        return responseList;
    }

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
                plateNumberList = getPlateNumberFromRequest(file, sender);
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
                plateNumberList = getPlateNumberFromRequest(file, sender);
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
                              @RequestPart(value = "image") MultipartFile file,
                              @RequestHeader(value = "Sender", required = false) String sender) {
        String response = "";
        if (authenticated(token)) {
            try {
                List<String> plateNumbers = getPlateNumberFromRequest(file, sender);
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
        if (/*authenticated(token)*/true) {
            List<String> plateNumberList = null;
            try {
                plateNumberList = getPlateNumberFromRequest(file, sender);
            } catch (Exception e) {
                e.printStackTrace();
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
                        if (iteratedCar.plateNumber.replace(" ", "")
                                .equalsIgnoreCase(plateNumber.replace(" ", ""))) {
                            selectedCar = iteratedCar;
                        }
                    }
                    if (selectedCar != null) {
                        databaseResponse.setCar(selectedCar);
                        databaseResponse.setStudent(responseStudent);
                        databaseResponse.setType(DatabaseResponse.Type.OK);
                    } else {
                        databaseResponse.setStudent(responseStudent);
                        databaseResponse.setType(DatabaseResponse.Type.STUDENTONLY);
                    }
                } else {
                    databaseResponse.setPlateNumber(plateNumber);
                    databaseResponse.setType(DatabaseResponse.Type.PLATEONLY);
                }
            } else {
                databaseResponse.setType(DatabaseResponse.Type.NOPLATES);
            }
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

    private void deleteOldestImage() {
        if (uploadDir.list().length > Integer.valueOf(Runner.properties.getProperty("maxImages", "200"))) {
            File oldest = null;
            for (File file : uploadDir.listFiles()) {
                if (oldest == null) {
                    oldest = file;
                } else {
                    if ((oldest.lastModified() > file.lastModified()) &&
                            (!file.getName().equals("latest.jpg") || !file.getName().equals("placeholder.jpg"))) {
                        oldest = file;
                    }
                }
            }
            if (oldest.exists()) {
                oldest.delete();
            }
        }
    }

}
