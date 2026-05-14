package ui;

public final class AppConfig {
    private static IconTheme selectedIconTheme = IconTheme.ANIMALS;

    private AppConfig() {
    }

    public static IconTheme getSelectedIconTheme() {
        return selectedIconTheme;
    }

    public static void setSelectedIconTheme(IconTheme theme) {
        if (theme != null) {
            selectedIconTheme = theme;
        }
    }
}