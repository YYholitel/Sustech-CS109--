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
    DifficultySelector difficultySelector;

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
        // 初始化顶部状态面板（包含计时、分数和状态文字）
        this.statusPanel = new StatusPanel(0, 0, 800, 100);
        this.statusPanel.setOnTimeUp(() -> endGame(false, "TIME"));
        GameBoard initialBoard = cellGenerator.generateBoard(size);
        boardPanel = new BoardPanel(initialBoard, 0, 100, 800, 800, statusPanel.getScores(), statusPanel,
                this::evaluateGameState);
        boardPanel.setGameOver(true);
        // 在状态面板顶部创建并放置难度选择控件及用户/存档显示，避免与 SaveManager/UserManager 的 UI 冲突
        this.difficultySelector = new DifficultySelector(120, 40, 10);
        int dsWidth = 120 * 2 + 10;
        int dsX = (800 - dsWidth) / 2;
        int dsY = 5; // 放置在 statusPanel 内的上方偏移
        difficultySelector.setBounds(dsX, dsY, dsWidth, 42);
        this.statusPanel.add(difficultySelector);

        // 添加用户信息面板（左侧）和存档面板（右侧）到 statusPanel 顶部
        UserInfoPanel userInfoPanel = new UserInfoPanel(this);
        userInfoPanel.setBounds(10, dsY, 180, 40);
        this.statusPanel.add(userInfoPanel);

        SaveSlotsPanel saveSlotsPanel = new SaveSlotsPanel(this);
        saveSlotsPanel.setBounds(800 - 200 - 10, dsY, 200, 40);
        this.statusPanel.add(saveSlotsPanel);

        // 将同一个 difficultySelector 传入 ControlPanel，确保只存在一个难度选择器
        this.controlPanel = new ControlPanel(statusPanel, 0, 900, 800, 100, difficultySelector, difficulty -> {
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

    // 供外部 UI（存档/用户面板）访问当前棋盘、状态和难度选择器
    public BoardPanel getBoardPanel() {
        return boardPanel;
    }

    public StatusPanel getStatusPanel() {
        return statusPanel;
    }

    public DifficultySelector getDifficultySelector() {
        return difficultySelector;
    }

}
