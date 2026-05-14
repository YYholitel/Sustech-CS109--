package ui;

import app.LevelProgressManager;
import logic.Difficulty;

import javax.swing.*;
import java.awt.*;

/**
 * 闯关模式选择页面：展示 6 个关卡方块，点击即可进入对应关卡。
 */
public class LevelSelectFrame extends JFrame {
  private final UiLayoutScaler layoutScaler = new UiLayoutScaler(700, 460);
  private final JPanel root = new JPanel(null);
  private final JPanel[] levelCards = new JPanel[6];

  public LevelSelectFrame() {
    super("连连看 - 闯关模式");
    setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    setSize(700, 460);
    setMinimumSize(new Dimension(620, 420));
    setLocationRelativeTo(null);

    root.setBackground(new Color(245, 247, 250));

    JLabel title = new JLabel("选择关卡", SwingConstants.CENTER);
    title.setFont(UiFont.font(Font.BOLD, 30));
    title.setBounds(0, 20, 700, 42);
    root.add(title);

    LevelProgressManager.syncToCurrentPlayer();
    buildLevelCards();

    ModernButton backButton = new ModernButton("返回主界面");
    backButton.setFont(UiFont.font(Font.PLAIN, 14));
    backButton.addActionListener(e -> {
      dispose();
      new InitFrame();
    });
    root.add(backButton);

    setContentPane(root);
    layoutComponents();
    addComponentListener(new java.awt.event.ComponentAdapter() {
      @Override
      public void componentResized(java.awt.event.ComponentEvent e) {
        layoutComponents();
      }
    });
    setVisible(true);
  }

  private void buildLevelCards() {
    Difficulty[] levels = Difficulty.values();
    for (int i = 0; i < levels.length; i++) {
      Difficulty level = levels[i];
      JPanel card = new JPanel(null);
      boolean unlocked = level.isUnlocked();
      if (unlocked) {
        card.setBackground(new Color(255, 255, 255));
        card.setBorder(BorderFactory.createLineBorder(new Color(220, 224, 230), 1));
      } else {
        card.setBackground(new Color(228, 232, 238));
        card.setBorder(BorderFactory.createLineBorder(new Color(178, 184, 194), 1));
      }

      JLabel levelLabel = new JLabel("第" + (i + 1) + "关", SwingConstants.CENTER);
      levelLabel.setFont(UiFont.font(Font.BOLD, 20));
      if (!unlocked) {
        levelLabel.setForeground(new Color(110, 118, 130));
      }
      levelLabel.setBounds(0, 16, 160, 30);
      card.add(levelLabel);

      if (unlocked) {
        ModernButton enterButton = new ModernButton("进入");
        enterButton.setFont(UiFont.font(Font.BOLD, 16));
        enterButton.addActionListener(e -> {
          dispose();
          GameFrame gameFrame = new GameFrame("连连看 - 闯关 第" + (level.ordinal() + 1) + "关", 800, 1000, level);
          gameFrame.repaint();
        });
        enterButton.setBounds(30, 98, 100, 40);
        card.add(enterButton);
      } else {
        JLabel lockIcon = new JLabel("🔒", SwingConstants.CENTER);
        lockIcon.setFont(UiFont.font(Font.BOLD, 18));
        lockIcon.setForeground(new Color(110, 118, 130));
        lockIcon.setBounds(0, 80, 160, 28);
        card.add(lockIcon);

        JLabel lockedText = new JLabel("未解锁", SwingConstants.CENTER);
        lockedText.setFont(UiFont.font(Font.BOLD, 16));
        lockedText.setForeground(new Color(110, 118, 130));
        lockedText.setBounds(0, 108, 160, 28);
        card.add(lockedText);
      }

      levelCards[i] = card;
      root.add(card);
    }
  }

  private void layoutComponents() {
    int currentWidth = Math.max(1, root.getWidth() > 0 ? root.getWidth() : getWidth());
    int currentHeight = Math.max(1, root.getHeight() > 0 ? root.getHeight() : getHeight());
    double scale = layoutScaler.getScaleFactor(currentWidth, currentHeight);

    Component[] components = root.getComponents();
    if (components.length < 8) {
      return;
    }

    components[0].setBounds(layoutScaler.scaleRect(0, 20, 700, 42, scale));

    int card = layoutScaler.scale(160, scale);
    int hGap = layoutScaler.scale(30, scale);
    int vGap = layoutScaler.scale(24, scale);
    int totalGridWidth = card * 3 + hGap * 2;
    int startX = (currentWidth - totalGridWidth) / 2;
    int startY = layoutScaler.scale(95, scale);

    for (int i = 0; i < 6; i++) {
      int row = i / 3;
      int col = i % 3;
      int x = startX + col * (card + hGap);
      int y = startY + row * (card + vGap);
      levelCards[i].setBounds(x, y, card, card);
    }

    int backWidth = layoutScaler.scale(130, scale);
    int backHeight = layoutScaler.scale(38, scale);
    int backX = (currentWidth - backWidth) / 2;
    int backY = Math.min(currentHeight - backHeight - layoutScaler.scale(18, scale),
        startY + 2 * card + vGap + layoutScaler.scale(12, scale));
    components[7].setBounds(backX, backY, backWidth, backHeight);

    root.repaint();
  }
}
