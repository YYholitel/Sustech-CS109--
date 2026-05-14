package ui;

import javax.swing.*;
import java.awt.*;

/**
 * 图标主题选择面板：负责显示和切换当前主题，不再直接承担窗口职责。
 */
public class IconType extends JPanel {
    private final ModernButton animalsButton;
    private final ModernButton iceCreamButton;
    private final ModernButton fruitsButton;
    private final JLabel resultLabel;

    public IconType() {
        this.setLayout(new FlowLayout(FlowLayout.CENTER, 12, 10));

        animalsButton = new ModernButton("可爱动物");
        iceCreamButton = new ModernButton(" 美味冰淇淋");
        fruitsButton = new ModernButton("Q弹水果");

        animalsButton.addActionListener(e -> setSelectedTheme(IconTheme.ANIMALS));
        iceCreamButton.addActionListener(e -> setSelectedTheme(IconTheme.ICE_CREAMS));
        fruitsButton.addActionListener(e -> setSelectedTheme(IconTheme.FRUITS));

        resultLabel = new JLabel();
        resultLabel.setFont(new Font("宋体", Font.BOLD, 14));

        add(animalsButton);
        add(iceCreamButton);
        add(fruitsButton);
        add(resultLabel);

        setSelectedTheme(AppConfig.getSelectedIconTheme());
    }

    public void setSelectedTheme(IconTheme theme) {
        IconTheme normalizedTheme = theme == null ? IconTheme.ANIMALS : theme;
        AppConfig.setSelectedIconTheme(normalizedTheme);
        updateButtonStates(normalizedTheme);
        showResult(normalizedTheme);
    }

    public IconTheme getSelectedTheme() {
        return AppConfig.getSelectedIconTheme();
    }

    private void updateButtonStates(IconTheme theme) {
        animalsButton.setBackground(theme == IconTheme.ANIMALS ? Color.GREEN : null);
        iceCreamButton.setBackground(theme == IconTheme.ICE_CREAMS ? Color.GREEN : null);
        fruitsButton.setBackground(theme == IconTheme.FRUITS ? Color.GREEN : null);
    }

    private void showResult(IconTheme theme) {
        resultLabel.setText("当前选择：" + theme.getDisplayName() + "主题");
        if (theme == IconTheme.ANIMALS) {
            resultLabel.setForeground(Color.BLUE);
        } else if (theme == IconTheme.ICE_CREAMS) {
            resultLabel.setForeground(Color.MAGENTA);
        } else {
            resultLabel.setForeground(Color.RED);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("选择图标类型");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(new IconType());
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}