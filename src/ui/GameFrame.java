package ui;

import logic.CellGenerator;
import logic.Difficulty;
import model.GameBoard;

import javax.swing.*;

//负责游戏的初始化和界面布局

public class GameFrame extends JFrame {
    int width;
    int height;
    String title;
    StatusPanel statusPanel;
    ControlPanel controlPanel;
    BoardPanel boardPanel;
    CellGenerator cellGenerator;
    int size;

    public GameFrame(String title, int width, int height) {
        super(title);
        this.setResizable(false);
        this.cellGenerator = new CellGenerator(Difficulty.Easy);
        this.size = getSizeForDifficulty(Difficulty.Easy);
        this.title = title;
        this.width = width;
        this.height = height;
        this.setLayout(null);
        this.setSize(width, height);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setVisible(true);
        this.statusPanel = new StatusPanel(0, 0, 800, 100);
        GameBoard initialBoard = cellGenerator.generateBoard(size);
        boardPanel = new BoardPanel(initialBoard, 0, 100, 800, 800, statusPanel.getScores());
        this.controlPanel = new ControlPanel(statusPanel, 0, 900, 800, 100, difficulty -> {
            cellGenerator.setDifficulty(difficulty);
            int newSize = getSizeForDifficulty(difficulty);
            GameBoard newBoard = cellGenerator.generateBoard(newSize);
            boardPanel.setGameBoard(newBoard);
        });
        this.add(this.statusPanel);
        this.add(this.controlPanel);
        this.add(boardPanel);
    }

    private int getSizeForDifficulty(Difficulty difficulty) {
        if (difficulty == Difficulty.Easy) {
            return 9;
        }
        return 10;
    }

}
