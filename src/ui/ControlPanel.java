package ui;

import javax.swing.*;
import java.awt.*;

public class ControlPanel extends JPanel {
    private static final UiLayoutScaler LAYOUT_SCALER = new UiLayoutScaler(800, 100);
    StatusPanel statusPanel;
    ModernButton backButton;
    ModernButton pauseButton;
    ModernButton undoButton;
    Runnable onPauseToggle;
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
         * @param onPauseToggle      暂停/继续回调
     * @param onUndo             撤销回调
     * @param boardPanel         棋盘面板引用，用于控制动画和获取游戏状态
     * @param gameFrame          游戏框架引用，用于检查游戏是否已结束
     */
    public ControlPanel(StatusPanel statusPanel, int offSetX, int offSetY, int width, int height,
             Runnable onPauseToggle, Runnable onUndo,
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
        this.backButton = new ModernButton("返回");
        this.pauseButton = new ModernButton("暂停");
        this.undoButton = new ModernButton("撤销");
        this.onPauseToggle = onPauseToggle;
        this.onUndo = onUndo;
        this.statusPanel = statusPanel;
        backButton.setFont(UiFont.font(Font.BOLD, 16));
        pauseButton.setFont(UiFont.font(Font.BOLD, 16));
        undoButton.setFont(UiFont.font(Font.BOLD, 16));
        this.add(backButton);
        this.add(pauseButton);
        this.add(undoButton);
        updateLayout(width, height);
        this.backButton.addActionListener(e -> {
            if (onBackToInit != null) {
                onBackToInit.run();
            }
        });
        this.pauseButton.addActionListener(e -> {
            if (gameFrame != null && gameFrame.isGameEnded()) {
                return;
            }
            if (onPauseToggle != null) {
                onPauseToggle.run();
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
        pauseButton.setBounds(baseX + btnWidth + gap, y, btnWidth, btnHeight);
        undoButton.setBounds(baseX + (btnWidth + gap) * 2, y, btnWidth, btnHeight);
        repaint();
    }

    public void updatePauseButtonText(boolean paused) {
        pauseButton.setText(paused ? "继续" : "暂停");
    }

}
