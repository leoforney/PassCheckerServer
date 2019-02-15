package tk.leoforney.passcheckerserver;

import com.google.gson.Gson;
import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import javaxt.io.Image;
import org.apache.commons.io.IOUtils;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

import static spark.Spark.get;
import static spark.Spark.post;
import static tk.leoforney.passcheckerserver.Main.wd;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * Created by Leo on 7/27/2018.
 */
public class PhotoManagement {

    Gson gson;
    Alpr alpr;
    PassManagement passManagement;
    File uploadDir;

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

        get("/plateNumber", (req, res) -> {
            System.out.println("Requested");
            return "<form method='post' enctype='multipart/form-data'>" // note the enctype
                    + "    <input type='file' name='image' accept='.jpg'>" // make sure to call getPart using the same "name" in the post
                    + "    <input type'text' name='token'>"
                    + "    <button>Upload picture</button>"
                    + "</form>";
        });


        post("/getStudent", (request, response) -> {
            if (authenticated(request)) {
                String plateNumber = getPlateNumberFromRequest(request);
                String student = passManagement.findStudentNameByPlateNumber(plateNumber);
                System.out.println("Student: " + student);
                return passManagement.findStudentByPlateNumber(plateNumber);
            }
            return "Not authenticated";
        });

        post("/getStudentName", (request, response) -> {
            if (authenticated(request)) {
                String plateNumber = getPlateNumberFromRequest(request);
                System.out.println(plateNumber);
                if (plateNumber.equals("No Plates Detected".toLowerCase())) {
                    return plateNumber;
                }
                return passManagement.findStudentNameByPlateNumber(plateNumber);
            }
            return "Not authenticated";
        });

        post("/plateNumber", (request, response) -> {
            //if (authenticated(request)) {
            return getPlateNumberFromRequest(request);
            //}
            //return "Not authenticated";
        });

        get("/checkInDatabase", (req, res) -> {
            System.out.println("Requested");
            return "<form method='post' enctype='multipart/form-data'>" // note the enctype
                    + "    <input type='file' name='image' accept='.jpg'>" // make sure to call getPart using the same "name" in the post
                    + "    <input type'text' name='token'>"
                    + "    <button>Upload picture</button>"
                    + "</form>";
        });

        post("/checkInDatabase", (request, response) -> {
            //if (authenticated(request)) {
            String plateNumber = getPlateNumberFromRequest(request);
            if (!plateNumber.toLowerCase().replace(" ", "").contains("noplatesdetected")) {
                Student responseStudent = passManagement.checkInStudent(plateNumber);
                if (responseStudent.name != null) {
                    Car selectedCar = null;
                    for (Car iteratedCar: responseStudent.cars) {
                        if (iteratedCar.plateNumber.replace(" ", "")
                                .equalsIgnoreCase(plateNumber.replace(" ", ""))) {
                            selectedCar = iteratedCar;
                        }
                    }
                    if (selectedCar != null) {
                        return gson.toJson(new DatabaseResponse(responseStudent, selectedCar));
                    }
                    return "Couldn't find car's owner";
                }
                return "No Students Detected";
            }
            return "No Plates Detected";
            //}
        });
    }

    public String getPlateNumberFromRequest(Request request) throws Exception {
        System.out.println("Picture received");

        File photoFile = new File(uploadDir.getAbsolutePath() + File.separator + System.currentTimeMillis()
                + ".jpg");

        if (photoFile.exists()) {
            photoFile.delete();
        }

        photoFile.createNewFile();

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));

        if (request.headers("Sender") != null) {
            Part part = request.raw().getPart("image");
            if (part.getContentType().toLowerCase().equals("image/yuv_420_88")) {

            } else if (part.getContentType().toLowerCase().equals("image/jpeg")) {
                try (InputStream input = part.getInputStream()) { // getPart needs to use same "name" as input field in form
                    byte[] resultByteData = IOUtils.toByteArray(input);
                    Image image = new Image(resultByteData);
                    image.rotateClockwise();
                    Files.write(photoFile.toPath(), image.getByteArray(), StandardOpenOption.WRITE);
                }
            }
        } else {
            try (InputStream input = request.raw().getPart("image").getInputStream()) { // getPart needs to use same "name" as input field in form
                if (!photoFile.exists()) photoFile.createNewFile();
                Files.copy(input, photoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
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

        return returnValue.toLowerCase();
    }

    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

}
