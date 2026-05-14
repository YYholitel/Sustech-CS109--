package ui;

import logic.Difficulty;

import javax.swing.*;
import java.awt.*;

/**
 * 难度选择器组件：提供 Level1~Level6 下拉选择。
 * 这是一个独立组件，可以被 `StatusPanel` 和 `ControlPanel` 复用，避免 UI 冲突。
 */
public class DifficultySelector extends JPanel {
  private final JLabel titleLabel;
  private final JComboBox<Difficulty> levelCombo;
  private Difficulty selectedDifficulty;
  private final int baseControlWidth;
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
    this.baseControlWidth = btnWidth;
    this.baseButtonHeight = btnHeight;
    this.baseGap = gap;
    this.titleLabel = new JLabel("关卡:");
    this.levelCombo = new JComboBox<>(Difficulty.values());
    this.selectedDifficulty = Difficulty.Level1;

    titleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    titleLabel.setFont(UiFont.font(Font.BOLD, 14));
    levelCombo.setFont(UiFont.font(Font.BOLD, 14));
    levelCombo.setSelectedItem(selectedDifficulty);
    levelCombo.addActionListener(e -> {
      Object selected = levelCombo.getSelectedItem();
      if (selected instanceof Difficulty) {
        selectedDifficulty = (Difficulty) selected;
      }
    });

    this.add(titleLabel);
    this.add(levelCombo);
    updateLayout(btnWidth, btnHeight);
  }

  public void updateLayout(int width, int height) {
    double scale = layoutScaler.getScaleFactor(Math.max(1, width), Math.max(1, height));
    int scaledControlWidth = layoutScaler.scale(baseControlWidth, scale);
    int scaledButtonHeight = layoutScaler.scale(baseButtonHeight, scale);
    int scaledGap = layoutScaler.scale(baseGap, scale);
    int labelWidth = layoutScaler.scale(58, scale);
    int comboWidth = Math.max(80, scaledControlWidth - labelWidth - scaledGap);
    titleLabel.setBounds(0, 0, labelWidth, scaledButtonHeight);
    levelCombo.setBounds(labelWidth + scaledGap, 0, comboWidth, scaledButtonHeight);
    setPreferredSize(new Dimension(labelWidth + scaledGap + comboWidth, scaledButtonHeight));
    repaint();
  }

  /** 返回当前被选中的难度 */
  public Difficulty getSelectedDifficulty() {
    return selectedDifficulty;
  }

  public void setSelectedDifficulty(Difficulty difficulty) {
    if (difficulty == null) {
      return;
    }
    selectedDifficulty = difficulty;
    levelCombo.setSelectedItem(difficulty);
  }

  public void setSelectorEnabled(boolean enabled) {
    levelCombo.setEnabled(enabled);
  }
}
