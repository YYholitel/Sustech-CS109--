package app;

import ui.InitFrame;
import ui.UiFont;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UiFont.installDefaults();
        SwingUtilities.invokeLater(InitFrame::new);
    }
}
