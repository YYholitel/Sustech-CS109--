package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;

/**
 * 难度选择器组件：提供 Easy / Hard 两个按钮供用户选择难度
 * 这是一个独立组件，可以被 `StatusPanel` 和 `ControlPanel` 复用，避免 UI 冲突。
 */
public class DifficultySelector extends JPanel {
  private final JButton easyButton;
  private final JButton hardButton;
  private Difficulty selectedDifficulty;

  /**
   * 构造函数只负责创建组件元素，位置和大小由外部通过 `setBounds` 设置
   * 
   * @param btnWidth  按钮宽度
   * @param btnHeight 按钮高度
   * @param gap       两按钮之间的间距
   */
  public DifficultySelector(int btnWidth, int btnHeight, int gap) {
    this.setLayout(null);
    this.easyButton = new JButton("Easy");
    this.hardButton = new JButton("Hard");
    this.selectedDifficulty = Difficulty.Easy;

    // 将按钮摆放在 (0,0) 原点，外部容器通过 setBounds 控制整体位置
    easyButton.setBounds(0, 0, btnWidth, btnHeight);
    hardButton.setBounds(btnWidth + gap, 0, btnWidth, btnHeight);

    easyButton.setFont(new Font("Arial", Font.BOLD, 20));
    hardButton.setFont(new Font("Arial", Font.BOLD, 20));

    easyButton.setFocusPainted(false);
    hardButton.setFocusPainted(false);

    this.add(easyButton);
    this.add(hardButton);

    // 选择处理
    easyButton.addActionListener(e -> selectedDifficulty = Difficulty.Easy);
    hardButton.addActionListener(e -> selectedDifficulty = Difficulty.Hard);
  }

  /** 返回当前被选中的难度 */
  public Difficulty getSelectedDifficulty() {
    return selectedDifficulty;
  }
}
