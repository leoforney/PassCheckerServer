package tk.leoforney.passcheckerserver;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import spark.Request;

import javax.servlet.MultipartConfigElement;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static spark.Spark.get;
import static spark.Spark.post;
import static tk.leoforney.passcheckerserver.Main.wd;
import static tk.leoforney.passcheckerserver.UserManagement.authenticated;

/**
 * Created by Leo on 7/27/2018.
 */
public class PhotoManagement {

    Alpr alpr;
    PassManagement passManagement;
    File uploadDir;

    public PhotoManagement(PassManagement passManagement) {
        this.passManagement = passManagement;

        uploadDir = new File(wd + File.separator + "upload");

        String os = System.getProperty("os.name");
        String username = System.getProperty("user.name");

        if (os.toLowerCase().contains("nix") || os.toLowerCase().contains("nux")) {
            alpr = new Alpr("us", "/etc/openalpr/openalpr.conf", "/usr/share/openalpr/runtime_data");
        } else {
            alpr = new Alpr("us", "C:/Users/" + username + "/openalpr/openalpr.conf", "C:/Users/" + username + "/openalpr/runtime_data");
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
    }

    public String getPlateNumberFromRequest(Request request) throws Exception {
        System.out.println("Picture received");

        Path tempFile = Files.createTempFile(uploadDir.toPath(), "", "");
        tempFile.toFile().delete();

        request.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        
        try (InputStream input = request.raw().getPart("image").getInputStream()) { // getPart needs to use same "name" as input field in form
            Files.copy(input, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }

        String returnValue = "";
        if (alpr.isLoaded()) {
            AlprResults results = alpr.recognize(tempFile.toString());
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
