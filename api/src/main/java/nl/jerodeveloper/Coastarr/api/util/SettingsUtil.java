package nl.jerodeveloper.coastarr.api.util;

import lombok.Getter;
import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.objects.Settings;

import java.io.*;

public class SettingsUtil {

    @Getter private Settings settings;

    public void loadSettings() throws IOException {
        File file = createSettingsFile();

        this.settings = Constants.INSTANCE.getGson().fromJson(new FileReader(file), Settings.class);
    }

    public void saveSettings() throws IOException {
        File file = createSettingsFile();

        OutputStream outputStream = new FileOutputStream(file);

        outputStream.write(Constants.INSTANCE.getGson().toJson(getSettings()).getBytes());
    }

    private File createSettingsFile() throws IOException {
        File file = new File("settings.json");

        if (!file.exists()) {
            if (!file.createNewFile()) {
                throw new RuntimeException("Something went wrong while creating settings file.");
            }
        }
        return file;
    }

}
