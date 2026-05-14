package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;

/**
 * 难度选择器组件：提供 Easy / Hard 两个按钮供用户选择难度
 * 这是一个独立组件，可以被 `StatusPanel` 和 `ControlPanel` 复用，避免 UI 冲突。
 */
public class DifficultySelector extends JPanel {
  private final ModernButton easyButton;
  private final ModernButton hardButton;
  private Difficulty selectedDifficulty;
  private final int baseButtonWidth;
  private final int baseButtonHeight;
  private final int baseGap;
  private final UiLayoutScaler layoutScaler = new UiLayoutScaler(250, 42);

  /**
   * 构造函数只负责创建组件元素，位置和大小由外部通过 `setBounds` 设置
   * 
   * @param btnWidth  按钮宽度
   * @param btnHeight 按钮高度
   * @param gap       两按钮之间的间距
   */
  public DifficultySelector(int btnWidth, int btnHeight, int gap) {
    this.setLayout(null);
    this.baseButtonWidth = btnWidth;
    this.baseButtonHeight = btnHeight;
    this.baseGap = gap;
    this.easyButton = new ModernButton("Easy");
    this.hardButton = new ModernButton("Hard");
    this.selectedDifficulty = Difficulty.Easy;

    // 将按钮摆放在 (0,0) 原点，外部容器通过 setBounds 控制整体位置
    easyButton.setBounds(0, 0, btnWidth, btnHeight);
    hardButton.setBounds(btnWidth + gap, 0, btnWidth, btnHeight);

    easyButton.setFont(UiFont.font(Font.BOLD, 14));
    hardButton.setFont(UiFont.font(Font.BOLD, 14));

    this.add(easyButton);
    this.add(hardButton);

    // 选择处理
    easyButton.addActionListener(e -> selectedDifficulty = Difficulty.Easy);
    hardButton.addActionListener(e -> selectedDifficulty = Difficulty.Hard);
  }

  public void updateLayout(int width, int height) {
    double scale = layoutScaler.getScaleFactor(Math.max(1, width), Math.max(1, height));
    int scaledButtonWidth = layoutScaler.scale(baseButtonWidth, scale);
    int scaledButtonHeight = layoutScaler.scale(baseButtonHeight, scale);
    int scaledGap = layoutScaler.scale(baseGap, scale);
    easyButton.setBounds(0, 0, scaledButtonWidth, scaledButtonHeight);
    hardButton.setBounds(scaledButtonWidth + scaledGap, 0, scaledButtonWidth, scaledButtonHeight);
    setPreferredSize(new Dimension(scaledButtonWidth * 2 + scaledGap, scaledButtonHeight));
    repaint();
  }

  /** 返回当前被选中的难度 */
  public Difficulty getSelectedDifficulty() {
    return selectedDifficulty;
  }
}
