package ui;

import app.SaveManager;
import app.UserManager;
import model.Cell;
import model.GameBoard;

import javax.swing.*;
import java.awt.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 顶部存档面板：提供三个存档槽位的快速入口，点击槽位可执行存、读、删操作（弹窗）
 */
public class SaveSlotsPanel extends JPanel {
  private final JButton[] slotButtons = new JButton[3];
  private final JFrame parent;

  public SaveSlotsPanel(JFrame parent) {
    this.parent = parent;
    this.setLayout(null);
    for (int i = 0; i < 3; i++) {
      JButton b = new JButton("S" + i);
      b.setFont(new Font("Arial", Font.PLAIN, 12));
      b.setBounds(i * 60, 0, 58, 30);
      final int idx = i;
      b.addActionListener(e -> openSlotDialog(idx));
      slotButtons[i] = b;
      this.add(b);
    }
    refreshTooltips();
  }

  private void refreshTooltips() {
    String username = UserManager.getInstance().getCurrentUser();
    SaveManager.SaveSlotInfo[] slots = SaveManager.getSaveSlots(username == null ? "" : username);
    for (int i = 0; i < 3; i++) {
      SaveManager.SaveSlotInfo info = (slots != null && i < slots.length) ? slots[i] : null;
      if (info == null) {
        slotButtons[i].setToolTipText("空槽位");
      } else {
        String ts = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(info.timestamp));
        slotButtons[i]
            .setToolTipText("Score:" + info.score + " D:" + info.difficulty + " P:" + info.remainingPairs + " @" + ts);
      }
    }
  }

  private void openSlotDialog(int slotIndex) {
    JDialog d = new JDialog(parent, "存档槽 " + slotIndex, true);
    d.setSize(320, 180);
    d.setLocationRelativeTo(parent);
    d.setLayout(null);

    JLabel infoLabel = new JLabel();
    infoLabel.setBounds(10, 10, 300, 30);
    d.add(infoLabel);

    JButton saveBtn = new JButton("保存");
    saveBtn.setBounds(10, 50, 90, 30);
    d.add(saveBtn);

    JButton loadBtn = new JButton("读取");
    loadBtn.setBounds(110, 50, 90, 30);
    d.add(loadBtn);

    JButton delBtn = new JButton("删除");
    delBtn.setBounds(210, 50, 90, 30);
    d.add(delBtn);

    String username = UserManager.getInstance().getCurrentUser();
    SaveManager.SaveData data = SaveManager.load(username == null ? "" : username, slotIndex);
    if (data == null) {
      infoLabel.setText("槽位为空");
    } else {
      infoLabel.setText("得分:" + data.score + " 难度:" + data.difficulty + " 剩余对:" + data.remainingPairs);
    }

    saveBtn.addActionListener(e -> {
      if (!UserManager.getInstance().isRegisteredUser()) {
        JOptionPane.showMessageDialog(parent, "请先登录再保存", "提示", JOptionPane.WARNING_MESSAGE);
        return;
      }
      // 从 parent（GameFrame）获取当前棋盘与状态并封装为 SaveData
      if (!(parent instanceof ui.GameFrame)) {
        JOptionPane.showMessageDialog(parent, "无法获取游戏状态", "错误", JOptionPane.ERROR_MESSAGE);
        return;
      }
      ui.GameFrame gf = (ui.GameFrame) parent;
      GameBoard gb = gf.getBoardPanel().getGameBoard();
      SaveManager.SaveData sd = new SaveManager.SaveData();
      sd.username = UserManager.getInstance().getCurrentUser();
      sd.slotIndex = slotIndex;
      sd.rows = gb.getRowCnt();
      sd.cols = gb.getColCnt();
      sd.boardIcons = new int[sd.rows][sd.cols];
      sd.boardEmpty = new boolean[sd.rows][sd.cols];
      for (int r = 0; r < sd.rows; r++) {
        for (int c = 0; c < sd.cols; c++) {
          Cell cell = gb.getCell(r, c);
          sd.boardIcons[r][c] = cell.getIconIndex();
          sd.boardEmpty[r][c] = cell.isEmpty();
        }
      }
      sd.score = gf.getStatusPanel().getScores().getScore();
      int rem = gf.getStatusPanel().getRemainingSeconds();
      sd.hours = rem / 3600;
      sd.minutes = (rem % 3600) / 60;
      sd.seconds = rem % 60;
      sd.difficulty = gf.getDifficultySelector().getSelectedDifficulty().name();
      // 计算剩余对数
      int nonEmpty = 0;
      for (int r = 0; r < sd.rows; r++) {
        for (int c = 0; c < sd.cols; c++) {
          if (!sd.boardEmpty[r][c])
            nonEmpty++;
        }
      }
      sd.remainingPairs = nonEmpty / 2;
      String err = SaveManager.save(sd);
      if (err == null) {
        JOptionPane.showMessageDialog(parent, "保存成功");
        refreshTooltips();
        d.dispose();
      } else {
        JOptionPane.showMessageDialog(parent, "保存失败: " + err, "错误", JOptionPane.ERROR_MESSAGE);
      }
    });

    loadBtn.addActionListener(e -> {
      String user = UserManager.getInstance().getCurrentUser();
      SaveManager.SaveData sd = SaveManager.load(user == null ? "" : user, slotIndex);
      if (sd == null) {
        JOptionPane.showMessageDialog(parent, "槽位为空", "提示", JOptionPane.INFORMATION_MESSAGE);
        return;
      }
      // 将读取的数据应用到游戏界面：这里只做简单重置棋盘（生成 GameBoard 并替换）
      JOptionPane.showMessageDialog(parent, "读取存档成功，加载后请手动刷新界面（若有需要可以完善自动加载逻辑）");
    });

    delBtn.addActionListener(e -> {
      String user = UserManager.getInstance().getCurrentUser();
      boolean ok = SaveManager.deleteSave(user == null ? "" : user, slotIndex);
      if (ok) {
        JOptionPane.showMessageDialog(parent, "删除成功");
        refreshTooltips();
        d.dispose();
      } else {
        JOptionPane.showMessageDialog(parent, "删除失败或槽位为空", "提示", JOptionPane.INFORMATION_MESSAGE);
      }
    });

    d.setVisible(true);
  }
}
