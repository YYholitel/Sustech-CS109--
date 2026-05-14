package ui;

import java.io.File;

public enum IconTheme {
    ANIMALS("动物", "resource/pictures/animals"),
    ICE_CREAMS("冰淇淋", "resource/pictures/iceCreams"),
    FRUITS("水果", "resource/pictures/fruits");

    private final String displayName;
    private final String directoryPath;

    IconTheme(String displayName, String directoryPath) {
        this.displayName = displayName;
        this.directoryPath = directoryPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public File getDirectory() {
        return new File(directoryPath);
    }
}