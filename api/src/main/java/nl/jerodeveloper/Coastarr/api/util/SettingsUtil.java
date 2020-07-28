package nl.jerodeveloper.coastarr.api.util;

import lombok.Getter;
import nl.jerodeveloper.coastarr.api.Constants;
import nl.jerodeveloper.coastarr.api.objects.Settings;

import java.io.*;
import java.util.Scanner;

public class SettingsUtil {

    @Getter private Settings settings;

    public void loadSettings() throws IOException {
        File file = createSettingsFile();
        Scanner scanner = new Scanner(file);
        StringBuilder stringBuilder = new StringBuilder();
        while (scanner.hasNextLine()) {
            stringBuilder.append(scanner.nextLine());
        }
        scanner.close();

        this.settings = Constants.INSTANCE.getGson().fromJson(stringBuilder.toString(), Settings.class);
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
