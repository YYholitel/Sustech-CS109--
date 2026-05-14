package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ControlPanel extends JPanel {
    private static final UiLayoutScaler LAYOUT_SCALER = new UiLayoutScaler(800, 100);
    StatusPanel statusPanel;
    ModernButton backButton;
    ModernButton startButton;
    ModernButton undoButton;
    DifficultySelector difficultySelector;
    Consumer<Difficulty> onStart;
    Runnable onUndo;
    Runnable onBackToInit;
    BoardPanel boardPanel;
    GameFrame gameFrame;
    int offSetX;
    int offSetY;
    int width;
    int height;

    /**
     * 构造 ControlPanel
     * 
     * @param statusPanel        顶部的状态面板引用，用于更新状态和计时
     * @param offSetX            面板 X 偏移
     * @param offSetY            面板 Y 偏移
     * @param width              面板宽度
     * @param height             面板高度
     * @param difficultySelector 外部注入的难度选择组件（避免与其它 UI 冲突）
     * @param onStart            游戏开始回调，接收选择的 Difficulty
     * @param onUndo             撤销回调
     * @param boardPanel         棋盘面板引用，用于控制动画和获取游戏状态
     * @param gameFrame          游戏框架引用，用于检查游戏是否已结束
     */
    public ControlPanel(StatusPanel statusPanel, int offSetX, int offSetY, int width, int height,
            DifficultySelector difficultySelector, Consumer<Difficulty> onStart, Runnable onUndo,
            Runnable onBackToInit, BoardPanel boardPanel, GameFrame gameFrame) {
        this.setLayout(null);
        this.setBounds(offSetX, offSetY, width, height);
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.width = width;
        this.height = height;
        this.onBackToInit = onBackToInit;
        this.boardPanel = boardPanel;
        this.gameFrame = gameFrame;
        this.backButton = new ModernButton("返回初始化");
        this.startButton = new ModernButton("开始");
        this.undoButton = new ModernButton("撤销");
        this.onStart = onStart;
        this.onUndo = onUndo;
        this.statusPanel = statusPanel;
        // 使用外部传入的难度选择器，确保界面上只存在一个难度选择区域
        this.difficultySelector = difficultySelector;
        backButton.setFont(UiFont.font(Font.BOLD, 16));
        startButton.setFont(UiFont.font(Font.BOLD, 16));
        undoButton.setFont(UiFont.font(Font.BOLD, 16));
        this.add(backButton);
        this.add(startButton);
        this.add(undoButton);
        updateLayout(width, height);
        this.backButton.addActionListener(e -> {
            if (onBackToInit != null) {
                onBackToInit.run();
            }
        });
        // 难度设置
        this.startButton.addActionListener(e -> {
            // 仅当已经开始过一次且当前游戏未结束时，才提示是否重新开始
            if (statusPanel.hasStarted() && !gameFrame.isGameEnded()) {
                int result = JOptionPane.showConfirmDialog(
                        this,
                        "游戏尚未结束，是否要重新开始呀",
                        "确认重新开始",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE);

                if (result != JOptionPane.YES_OPTION) {
                    return; // 用户选择"否", 不重新开始
                }
            }

            // 启动游戏
            statusPanel.setStatus("RUN");
            // 重置分数和连对计数，确保新游戏从零开始
            statusPanel.getScores().resetAll();
            // 启动倒计时
            Difficulty selected = difficultySelector.getSelectedDifficulty();
            statusPanel.startCountdown(selected.getTotalSeconds());
            if (onStart != null) {
                onStart.accept(selected);
            }

            // 启动棋子掉落动画
            if (boardPanel != null) {
                boardPanel.startDroppingAnimation();
            }
        });

        this.undoButton.addActionListener(e -> {
            if (onUndo != null) {
                onUndo.run();
            }
        });
    }

    // 注意：难度选择器已提取为独立组件 `DifficultySelector`，以便在多个面板间复用并避免冲突。

    public void updateLayout(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        double scale = LAYOUT_SCALER.getScaleFactor(this.width, this.height);

        int btnWidth = LAYOUT_SCALER.scale(130, scale);
        int btnHeight = LAYOUT_SCALER.scale(50, scale);
        int gap = LAYOUT_SCALER.scale(14, scale);
        int totalWidth = btnWidth * 3 + gap * 2;
        int baseX = (this.width - totalWidth) / 2;
        int y = (this.height - btnHeight) / 2;

        backButton.setBounds(baseX, y, btnWidth, btnHeight);
        startButton.setBounds(baseX + btnWidth + gap, y, btnWidth, btnHeight);
        undoButton.setBounds(baseX + (btnWidth + gap) * 2, y, btnWidth, btnHeight);
        repaint();
    }

}
