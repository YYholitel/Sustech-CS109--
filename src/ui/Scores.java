package ui;

import javax.swing.*;
import java.awt.*;

public class Scores extends JPanel {
  private final JLabel scoreLabel;
  private final JLabel comboLabel;
  private int score;
  private int combo;

  public Scores() {
    this.setLayout(new GridLayout(2, 1));
    this.scoreLabel = new JLabel("Score: 0");
    this.comboLabel = new JLabel("Combo: 0");
    Font font = UiFont.font(Font.BOLD, 24);
    scoreLabel.setFont(font);
    comboLabel.setFont(font);
    this.add(scoreLabel);
    this.add(comboLabel);
  }

  // 连胜机制

  public int onPairCleared() {
    combo++;
    int points = 10;
    if (combo > 2) {
      points += (5 * combo - 10);
    }
    score += points;
    updateLabels();
    return points;
  }

  // 清理连胜
  public void resetCombo() {
    if (combo != 0) {
      combo = 0;
      updateLabels();
    }
  }

  // 重置分数和连胜（新游戏时调用）
  public void resetAll() {
    score = 0;
    combo = 0;
    updateLabels();
  }

  public int getScore() {
    return score;
  }

  public int getCombo() {
    return combo;
  }

  public void restoreState(int score, int combo) {
    this.score = score;
    this.combo = combo;
    updateLabels();
  }

  public void applyUndoPenalty() {
    score = (score * 9) / 10;
    updateLabels();
  }

  private void updateLabels() {
    scoreLabel.setText("Score: " + score);
    comboLabel.setText("Combo: " + combo);
  }
}
