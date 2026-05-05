package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ControlPanel extends JPanel {
    StatusPanel statusPanel;
    JButton startButton;
    DifficultySelector difficultySelector;
    Consumer<Difficulty> onStart;
    int offSetX;
    int offSetY;
    int width;
    int height;

    public ControlPanel(StatusPanel statusPanel, int offSetX, int offSetY, int width, int height,
            Consumer<Difficulty> onStart) {
        this.setLayout(null);
        this.setBounds(offSetX, offSetY, width, height);
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.width = width;
        this.height = height;
        this.startButton = new JButton("start");
        this.onStart = onStart;
        this.statusPanel = statusPanel;
        int btnWidth = 150;
        int btnHeight = 50;
        int gap = 20;
        int totalWidth = btnWidth * 3 + gap * 2;
        int baseX = (width - totalWidth) / 2;
        int y = (height - btnHeight) / 2;

        difficultySelector = new DifficultySelector(
                baseX,
                y,
                btnWidth,
                btnHeight,
                gap);
        difficultySelector.setBounds(baseX, y, btnWidth * 2 + gap, btnHeight);
        startButton.setBounds(baseX + (btnWidth + gap) * 2, y, btnWidth, btnHeight);

        startButton.setFont(new Font("Arial", Font.BOLD, 25));

        startButton.setFocusPainted(false);
        this.add(difficultySelector);
        this.add(startButton);
        // 难度设置
        this.startButton.addActionListener(e -> {
            statusPanel.setStatus("RUN");
            // 重置分数和连对计数，确保新游戏从零开始
            statusPanel.getScores().resetAll();
            // 重置计时器
            statusPanel.resetTimer();
            if (onStart != null) {
                onStart.accept(difficultySelector.getSelectedDifficulty());
            }
        });
    }

    private static class DifficultySelector extends JPanel {
        private final JButton easyButton;
        private final JButton hardButton;
        private Difficulty selectedDifficulty;

        // 难度选择组件
        DifficultySelector(int x, int y, int btnWidth, int btnHeight, int gap) {
            this.setLayout(null);
            this.easyButton = new JButton("Easy");
            this.hardButton = new JButton("Hard");
            this.selectedDifficulty = Difficulty.Easy;

            easyButton.setBounds(0, 0, btnWidth, btnHeight);
            hardButton.setBounds(btnWidth + gap, 0, btnWidth, btnHeight);

            easyButton.setFont(new Font("Arial", Font.BOLD, 20));
            hardButton.setFont(new Font("Arial", Font.BOLD, 20));

            easyButton.setFocusPainted(false);
            hardButton.setFocusPainted(false);

            this.add(easyButton);
            this.add(hardButton);

            easyButton.addActionListener(e -> selectedDifficulty = Difficulty.Easy);
            hardButton.addActionListener(e -> selectedDifficulty = Difficulty.Hard);
        }

        Difficulty getSelectedDifficulty() {
            return selectedDifficulty;
        }
    }

}
