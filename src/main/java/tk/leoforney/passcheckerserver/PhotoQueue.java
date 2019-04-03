package tk.leoforney.passcheckerserver;

import com.openalpr.jni.Alpr;
import com.openalpr.jni.AlprPlate;
import com.openalpr.jni.AlprPlateResult;
import com.openalpr.jni.AlprResults;
import javaxt.io.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

import static tk.leoforney.passcheckerserver.PhotoManagement.uploadDir;

public class PhotoQueue {

    private static PhotoQueue instance = null;

    public static PhotoQueue getInstance() {
        if (instance == null) {
            instance = new PhotoQueue();
        }
        return instance;
    }

    public Alpr getAlpr() {
        return alpr;
    }

    public void setAlpr(Alpr alpr) {
        this.alpr = alpr;
    }

    private Alpr alpr;

    private PhotoQueue() {
        String os = System.getProperty("os.name");
        String username = System.getProperty("user.name");

        if (os.toLowerCase().contains("nix") || os.toLowerCase().contains("nux")) {
            alpr = new Alpr("us", "/etc/openalpr/openalpr.conf", "/usr/share/openalpr/runtime_data");
        } else {
            alpr = new Alpr("us", "C:/Users/" + username + "/openalpr/openalpr.conf", "C:/Users/" + username + "/openalpr/runtime_data/");
        }
        alpr.setTopN(3);
        alpr.setDefaultRegion("il");
    }

    private boolean latestInUse = false;

    public List<String> request(MultipartFile file, String sender) {
        try {
            return request(file.getBytes(), sender);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    boolean alprInUse = false;

    public List<String> request(byte[] data, String sender) throws Exception {
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

        if (sender != null) {
            if (sender.equals("Mobile(NoRotate)")) {
                Files.write(photoFile.toPath(), data, StandardOpenOption.WRITE);
            }
            if (sender.equals("Mobile(Rotate)")) {
                Image image = new Image(data);
                image.rotateClockwise();
                Files.write(photoFile.toPath(), image.getByteArray(), StandardOpenOption.WRITE);
            }
            if (!latestInUse) {
                latestInUse = true;
                Files.copy(photoFile.toPath(), latestPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
                latestInUse = false;
            }
        } else {
            try (InputStream input = new ByteArrayInputStream(data)) { // getPart needs to use same "name" as input field in form
                if (!photoFile.exists()) photoFile.createNewFile();
                Files.copy(input, photoFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                if (!latestInUse) {
                    latestInUse = true;
                    Files.copy(photoFile.toPath(), latestPhoto.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    latestInUse = false;
                }
            }
        }


        String returnValue = "";
        if (alpr.isLoaded() && !alprInUse) {
            alprInUse = true;
            AlprResults results = alpr.recognize(data);
            System.out.format("  %-15s%-8s\n", "Plate Number", "Confidence");
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
            alprInUse = false;
        }
        if (returnValue.equals("No Plates Detected")) {
            photoFile.delete();
        }

        deleteOldestImage();

        responseList.add(returnValue.toLowerCase()); // 0 - Plate number (lowercase)
        responseList.add(timestamp); // 1 - timestamp as string
        return responseList;
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

    public void close() {
        alpr.unload();
    }


}
