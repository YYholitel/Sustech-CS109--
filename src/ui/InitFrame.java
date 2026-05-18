package ui;

import app.LoginFrame;
import logic.Difficulty;

import javax.swing.*;
import java.awt.*;

public class InitFrame extends JFrame {
    public InitFrame() {
        super("连连看 - 回到大厅");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(560, 360));
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("连连看", SwingConstants.CENTER);
        titleLabel.setFont(UiFont.font(Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(24, 12, 12, 12));
        root.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        IconType iconTypePanel = new IconType();
        JPanel wrapper = new JPanel(new FlowLayout(FlowLayout.CENTER));
        wrapper.setOpaque(false);
        wrapper.add(iconTypePanel);
        centerPanel.add(wrapper, BorderLayout.CENTER);

        root.add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 12));
        bottomPanel.setOpaque(false);

        ModernButton continueButton = new ModernButton("进入登录");
        continueButton.setFont(UiFont.font(Font.BOLD, 14));
        continueButton.addActionListener(e -> {
            dispose();
            new LoginFrame();
        });

        ModernButton challengeButton = new ModernButton("闯关模式");
        challengeButton.setFont(UiFont.font(Font.BOLD, 14));
        challengeButton.addActionListener(e -> {
            dispose();
            new LevelSelectFrame();
        });

        ModernButton rankingButton = new ModernButton("排行榜");
        rankingButton.setFont(UiFont.font(Font.BOLD, 14));
        rankingButton.addActionListener(e -> RankingFrame.showRanking());

        ModernButton exitButton = new ModernButton("退出");
        exitButton.setFont(UiFont.font(Font.PLAIN, 14));
        exitButton.addActionListener(e -> System.exit(0));

        bottomPanel.add(continueButton);
        bottomPanel.add(challengeButton);
        bottomPanel.add(rankingButton);
        bottomPanel.add(exitButton);

        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }
}