package ui;

import app.LoginFrame;

import javax.swing.*;
import java.awt.*;

public class InitFrame extends JFrame {
    public InitFrame() {
        super("连连看 - 初始化");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(560, 360);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(560, 360));
        setResizable(true);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(new Color(245, 247, 250));

        JLabel titleLabel = new JLabel("选择游戏模式", SwingConstants.CENTER);
        titleLabel.setFont(UiFont.font(Font.BOLD, 28));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(24, 12, 12, 12));
        root.add(titleLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setOpaque(false);

        JLabel tipLabel = new JLabel("先选择图标主题，再进入登录界面", SwingConstants.CENTER);
        tipLabel.setFont(UiFont.font(Font.PLAIN, 16));
        tipLabel.setBorder(BorderFactory.createEmptyBorder(0, 12, 10, 12));
        centerPanel.add(tipLabel, BorderLayout.NORTH);

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

        ModernButton exitButton = new ModernButton("退出");
        exitButton.setFont(UiFont.font(Font.PLAIN, 14));
        exitButton.addActionListener(e -> System.exit(0));

        bottomPanel.add(continueButton);
        bottomPanel.add(exitButton);
        root.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(root);
        setVisible(true);
    }
}