package tk.leoforney.passcheckerserver;

import com.vaadin.flow.server.InputStreamFactory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ImageInputStream implements InputStreamFactory {

    File file;
    List<InputStream> inputStreamList;

    public ImageInputStream(File file) {
        this.file = file;
        inputStreamList = new ArrayList<>();
    }

    @Override
    public InputStream createInputStream() {
        InputStream stream = null;
        try {
            stream = FileUtils.openInputStream(file);
            inputStreamList.add(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream;
    }

    public void closeAllStreams() {
        for (InputStream iteratedStream: inputStreamList) {
            try {
                iteratedStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
