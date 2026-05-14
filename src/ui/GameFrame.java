package ui;

import logic.CellGenerator;
import logic.Difficulty;
import model.GameBoard;
import utils.Utils;

import javax.swing.*;
import java.awt.*;

//负责游戏的初始化和界面布局

public class GameFrame extends JFrame {
    private static final UiLayoutScaler LAYOUT_SCALER = new UiLayoutScaler(800, 1000);
    int width;
    int height;
    String title;
    StatusPanel statusPanel;
    UserInfoPanel userInfoPanel;
    SaveSlotsPanel saveSlotsPanel;

    ControlPanel controlPanel;
    BoardPanel boardPanel;
    CellGenerator cellGenerator;
    int size;
    boolean gameEnded = false;
    DifficultySelector difficultySelector;

    public GameFrame(String title, int width, int height) {
        super(title);
        this.setResizable(true);
        this.cellGenerator = new CellGenerator(Difficulty.Easy);
        this.size = getSizeForDifficulty(Difficulty.Easy);
        this.title = title;
        this.width = width;
        this.height = height;
        this.setLayout(null);
        this.setSize(width, height);
        this.setMinimumSize(new Dimension(700, 850));
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // 初始化顶部状态面板（包含计时、分数和状态文字）
        this.statusPanel = new StatusPanel(0, 0, 800, 100);
        this.statusPanel.setOnTimeUp(() -> endGame(false, "TIME"));
        GameBoard initialBoard = cellGenerator.generateBoard(size);
        boardPanel = new BoardPanel(initialBoard, 0, 100, 800, 800, statusPanel.getScores(), statusPanel,
                this::evaluateGameState);
        boardPanel.setGameOver(true);
        // 在状态面板顶部创建并放置难度选择控件及用户/存档显示，避免与 SaveManager/UserManager 的 UI 冲突
        this.difficultySelector = new DifficultySelector(120, 40, 10);
        this.statusPanel.add(difficultySelector);

        // 添加用户信息面板（左侧）和存档面板（右侧）到 statusPanel 顶部
        this.userInfoPanel = new UserInfoPanel(this);
        this.statusPanel.add(this.userInfoPanel);

        this.saveSlotsPanel = new SaveSlotsPanel(this);
        this.statusPanel.add(this.saveSlotsPanel);
        // 强制将顶部三个控件置顶，避免被状态文本/分数区域覆盖
        this.statusPanel.setComponentZOrder(this.difficultySelector, 0);
        this.statusPanel.setComponentZOrder(this.userInfoPanel, 0);
        this.statusPanel.setComponentZOrder(this.saveSlotsPanel, 0);

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
        }, this::returnToInit, boardPanel, this);
        this.add(this.statusPanel);
        this.add(this.controlPanel);
        this.add(boardPanel);
        layoutGameUi();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutGameUi();
            }
        });
        this.setVisible(true);
    }

    private void layoutGameUi() {
        int contentWidth = getContentPane().getWidth();
        int contentHeight = getContentPane().getHeight();
        if (contentWidth <= 0) {
            contentWidth = getWidth();
        }
        if (contentHeight <= 0) {
            contentHeight = getHeight();
        }
        contentWidth = Math.max(1, contentWidth);
        contentHeight = Math.max(1, contentHeight);
        double scale = LAYOUT_SCALER.getScaleFactor(contentWidth, contentHeight);
        int layoutWidth = LAYOUT_SCALER.scale(800, scale);
        int layoutHeight = LAYOUT_SCALER.scale(1000, scale);
        int offsetX = (contentWidth - layoutWidth) / 2;
        int offsetY = (contentHeight - layoutHeight) / 2;

        int statusHeight = LAYOUT_SCALER.scale(100, scale);
        int boardHeight = LAYOUT_SCALER.scale(800, scale);
        int controlHeight = Math.max(1, layoutHeight - statusHeight - boardHeight);

        statusPanel.setBounds(offsetX, offsetY, layoutWidth, statusHeight);
        statusPanel.updateLayout(layoutWidth, statusHeight);
        layoutStatusTopBar(layoutWidth, statusHeight);

        boardPanel.setBoardBounds(offsetX, offsetY + statusHeight, layoutWidth, boardHeight);

        controlPanel.setBounds(offsetX, offsetY + statusHeight + boardHeight, layoutWidth, controlHeight);
        controlPanel.updateLayout(layoutWidth, controlHeight);

        revalidate();
        repaint();
    }

    private void layoutStatusTopBar(int statusWidth, int statusHeight) {
        double scale = LAYOUT_SCALER.getScaleFactor(statusWidth, statusHeight);
        int dsWidth = LAYOUT_SCALER.scale(250, scale);
        int dsHeight = LAYOUT_SCALER.scale(42, scale);
        int dsX = LAYOUT_SCALER.centerX(statusWidth, 250, scale);
        int dsY = LAYOUT_SCALER.scale(2, scale);

        difficultySelector.setBounds(dsX, dsY, dsWidth, dsHeight);
        difficultySelector.updateLayout(dsWidth, dsHeight);

        int userWidth = LAYOUT_SCALER.scale(185, scale);
        int userHeight = LAYOUT_SCALER.scale(30, scale);
        userInfoPanel.setBounds(LAYOUT_SCALER.scale(10, scale), dsY, userWidth, userHeight);
        userInfoPanel.updateLayout(userWidth, userHeight);

        int saveWidth = LAYOUT_SCALER.scale(180, scale);
        int saveHeight = LAYOUT_SCALER.scale(30, scale);
        saveSlotsPanel.setBounds(statusWidth - saveWidth - LAYOUT_SCALER.scale(10, scale), dsY, saveWidth, saveHeight);
        saveSlotsPanel.updateLayout(saveWidth, saveHeight);
    }

    private void returnToInit() {
        statusPanel.stopTimer();
        dispose();
        new InitFrame();
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

    /**
     * 获取游戏是否已经结束
     */
    public boolean isGameEnded() {
        return gameEnded;
    }

}
