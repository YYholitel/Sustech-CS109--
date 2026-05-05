package app;

import model.Cell;
import model.GameBoard;
import model.Position;
import ui.BoardPanel;
import ui.GameFrame;

import javax.swing.*;

public class Main {
    JFrame loginFrame;
public static void run (){
    SwingUtilities.invokeLater(() -> {
        GameFrame frame = new GameFrame("连连看", 800, 1000);
        frame.repaint();
    });
}


    public static void main(String[] args) {
        run();  
    }
}

