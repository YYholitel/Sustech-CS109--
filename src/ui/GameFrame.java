package ui;

import logic.CellGenerator;
import logic.Difficulty;
import model.GameBoard;
import utils.Utils;

import javax.swing.*;
import java.awt.*;

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
    boolean gameEnded = false;

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
        this.statusPanel.setOnTimeUp(() -> endGame(false, "TIME"));
        GameBoard initialBoard = cellGenerator.generateBoard(size);
        boardPanel = new BoardPanel(initialBoard, 0, 100, 800, 800, statusPanel.getScores(), statusPanel,
                this::evaluateGameState);
        boardPanel.setGameOver(true);
        this.controlPanel = new ControlPanel(statusPanel, 0, 900, 800, 100, difficulty -> {
            cellGenerator.setDifficulty(difficulty);
            int newSize = getSizeForDifficulty(difficulty);
            GameBoard newBoard = cellGenerator.generateBoard(newSize);
            boardPanel.setGameBoard(newBoard);
            gameEnded = false;
            boardPanel.setGameOver(false);
            evaluateGameState();
        }, () -> {
            if (!boardPanel.undoLastClear()) {
                Toolkit.getDefaultToolkit().beep();
            }
        });
        this.add(this.statusPanel);
        this.add(this.controlPanel);
        this.add(boardPanel);
    }

    private void evaluateGameState() {
        if (gameEnded) {
            return;
        }
        GameBoard board = boardPanel.getGameBoard();
        int remaining = statusPanel.getRemainingSeconds();
        if (remaining <= 0) {
            endGame(false, "TIME");
            return;
        }
        if (!Utils.hasNonEmptyCell(board)) {
            endGame(true, "CLEAR");
            return;
        }
        if (!Utils.hasAnyValidMove(board)) {
            endGame(false, "NO_MOVES");
        }
    }

    private void endGame(boolean win, String reason) {
        if (gameEnded) {
            return;
        }
        gameEnded = true;
        statusPanel.stopTimer();
        boardPanel.setGameOver(true);
        if (win) {
            statusPanel.setStatus("WIN");
            JOptionPane.showMessageDialog(this, "胜利！", "胜利", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        statusPanel.setStatus("LOSE");
        if ("TIME".equals(reason)) {
            JOptionPane.showMessageDialog(this, "时间到，游戏失败！", "失败", JOptionPane.ERROR_MESSAGE);
        } else if ("NO_MOVES".equals(reason)) {
            JOptionPane.showMessageDialog(this, "无可消除路径，游戏失败！", "失败", JOptionPane.ERROR_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "游戏失败！", "失败", JOptionPane.ERROR_MESSAGE);
        }
    }

    private int getSizeForDifficulty(Difficulty difficulty) {
        if (difficulty == Difficulty.Easy) {
            return 9;
        }
        return 10;
    }

}
