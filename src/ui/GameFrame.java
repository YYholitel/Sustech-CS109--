package ui;

import app.LevelProgressManager;
import app.UserManager;
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
    boolean paused = false;
    DifficultySelector difficultySelector;
    String levelInfoText = "";
    Difficulty currentLevel;

    public GameFrame(String title, int width, int height) {
        this(title, width, height, Difficulty.Level1);
    }

    public GameFrame(String title, int width, int height, Difficulty initialDifficulty) {
        super(title);
        this.setResizable(true);
        this.cellGenerator = new CellGenerator(initialDifficulty);
        this.size = getSizeForDifficulty(initialDifficulty);
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
        this.difficultySelector = new DifficultySelector(250, 40, 10);
        this.difficultySelector.setSelectedDifficulty(initialDifficulty);
        this.difficultySelector.setSelectorEnabled(false);
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
        this.controlPanel = new ControlPanel(statusPanel, 0, 900, 800, 100, this::togglePauseResume, () -> {
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
        startLevel(initialDifficulty);
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
        if (gameEnded || paused) {
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
        paused = false;
        statusPanel.stopTimer();
        boardPanel.setGameOver(true);
        boardPanel.setPaused(false);
        controlPanel.updatePauseButtonText(false);

        if (win) {
            // 计算用时（总时间 - 剩余时间）
            int totalSeconds = currentLevel.getTotalSeconds();
            int remaining = statusPanel.getRemainingSeconds();
            int usedSeconds = totalSeconds - remaining;
            int score = statusPanel.getScores().getScore();

            // 记录成绩到用户数据
            UserManager.getInstance().addGameRecord(currentLevel.name(), usedSeconds, score);

            LevelProgressManager.onLevelCleared(currentLevel);
            statusPanel.setStatus("WIN");
            int nextIndex = currentLevel.ordinal() + 2;
            if (nextIndex <= Difficulty.values().length) {
                JOptionPane.showMessageDialog(this,
                        "胜利！已解锁第" + nextIndex + "关\n用时: " + formatTime(usedSeconds) + "  得分: " + score,
                        "胜利",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "胜利！已通关全部关卡\n用时: " + formatTime(usedSeconds) + "  得分: " + score,
                        "胜利",
                        JOptionPane.INFORMATION_MESSAGE);
            }
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

    // 添加辅助方法格式化时间
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        if (minutes > 0) {
            return String.format("%d分%d秒", minutes, secs);
        }
        return String.format("%d秒", secs);
    }

    private int getSizeForDifficulty(Difficulty difficulty) {
        // 前三关与第一关统一使用 9 的尺寸（内部座标范围 size+2），其它关卡使用 10
        if (difficulty == Difficulty.Level1 || difficulty == Difficulty.Level2 || difficulty == Difficulty.Level3) {
            return 9;
        }
        return 10;
    }

    private void startLevel(Difficulty difficulty) {
        currentLevel = difficulty;
        difficultySelector.setSelectedDifficulty(difficulty);
        cellGenerator.setDifficulty(difficulty);

        int newSize = getSizeForDifficulty(difficulty);
        GameBoard newBoard = cellGenerator.generateBoard(newSize);
        boardPanel.setGameBoard(newBoard);

        gameEnded = false;
        paused = false;
        boardPanel.setGameOver(false);
        boardPanel.setPaused(false);
        statusPanel.getScores().resetAll();
        statusPanel.startCountdown(difficulty.getTotalSeconds());

        int levelIndex = difficulty.ordinal() + 1;
        int pieceCount = countPieces(newBoard);
        int limitSeconds = difficulty.getTotalSeconds();
        levelInfoText = "关卡" + levelIndex + " | 棋子数量" + pieceCount + " | 限时间" + limitSeconds + "秒";
        // 不再在顶部显示重复的棋子数量/限时文案，改为在棋盘中央显示覆盖层
        java.util.Set<Integer> types = new java.util.HashSet<>();
        for (int r = 0; r < newBoard.getRowCnt(); r++) {
            for (int c = 0; c < newBoard.getColCnt(); c++) {
                model.Cell cell = newBoard.getCell(r, c);
                if (cell != null && !cell.isEmpty()) {
                    types.add(cell.getIconIndex());
                }
            }
        }
        int distinctTypes = types.size();
        if (boardPanel != null) {
            String overlay = "棋子: " + pieceCount + "   种类: " + distinctTypes + "   限时: " + limitSeconds + "秒";
            boardPanel.showLevelOverlay(overlay);
        }
        controlPanel.updatePauseButtonText(false);

        boardPanel.startDroppingAnimation();
        evaluateGameState();
    }

    private int countPieces(GameBoard board) {
        int count = 0;
        for (int r = 0; r < board.getRowCnt(); r++) {
            for (int c = 0; c < board.getColCnt(); c++) {
                if (!board.getCell(r, c).isEmpty()) {
                    count++;
                }
            }
        }
        return count;
    }

    private void togglePauseResume() {
        if (gameEnded) {
            return;
        }
        if (paused) {
            paused = false;
            statusPanel.resumeCountdown();
            boardPanel.setPaused(false);
            statusPanel.setStatus(levelInfoText);
            controlPanel.updatePauseButtonText(false);
            return;
        }
        paused = true;
        statusPanel.pauseCountdown();
        boardPanel.setPaused(true);
        statusPanel.setStatus("PAUSE");
        controlPanel.updatePauseButtonText(true);
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
