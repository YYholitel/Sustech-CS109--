package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ControlPanel extends JPanel {
    StatusPanel statusPanel;
    JButton startButton;
    JButton undoButton;
    DifficultySelector difficultySelector;
    Consumer<Difficulty> onStart;
    Runnable onUndo;
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
     */
    public ControlPanel(StatusPanel statusPanel, int offSetX, int offSetY, int width, int height,
            DifficultySelector difficultySelector, Consumer<Difficulty> onStart, Runnable onUndo) {
        this.setLayout(null);
        this.setBounds(offSetX, offSetY, width, height);
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.width = width;
        this.height = height;
        this.startButton = new JButton("start");
        this.undoButton = new JButton("undo");
        this.onStart = onStart;
        this.onUndo = onUndo;
        this.statusPanel = statusPanel;
        int btnWidth = 150;
        int btnHeight = 50;
        int gap = 20;
        int totalWidth = btnWidth * 4 + gap * 3;
        int baseX = (width - totalWidth) / 2;
        int y = (height - btnHeight) / 2;

        // 使用外部传入的难度选择器，确保界面上只存在一个难度选择区域
        this.difficultySelector = difficultySelector;
        difficultySelector.setBounds(baseX, y, btnWidth * 2 + gap, btnHeight);
        startButton.setBounds(baseX + (btnWidth + gap) * 2, y, btnWidth, btnHeight);
        undoButton.setBounds(baseX + (btnWidth + gap) * 3, y, btnWidth, btnHeight);

        startButton.setFont(new Font("Arial", Font.BOLD, 25));
        undoButton.setFont(new Font("Arial", Font.BOLD, 25));

        startButton.setFocusPainted(false);
        undoButton.setFocusPainted(false);
        this.add(difficultySelector);
        this.add(startButton);
        this.add(undoButton);
        // 难度设置
        this.startButton.addActionListener(e -> {
            statusPanel.setStatus("RUN");
            // 重置分数和连对计数，确保新游戏从零开始
            statusPanel.getScores().resetAll();
            // 启动倒计时
            Difficulty selected = difficultySelector.getSelectedDifficulty();
            statusPanel.startCountdown(selected.getTotalSeconds());
            if (onStart != null) {
                onStart.accept(selected);
            }
        });

        this.undoButton.addActionListener(e -> {
            if (onUndo != null) {
                onUndo.run();
            }
        });
    }

    // 注意：难度选择器已提取为独立组件 `DifficultySelector`，以便在多个面板间复用并避免冲突。

}
