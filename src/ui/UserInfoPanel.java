package ui;

import app.LoginFrame;
import app.UserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 顶部用户信息面板：显示当前用户名并提供登录/登出按钮
 */
public class UserInfoPanel extends JPanel {
  private final JLabel userLabel;
  private final JButton authButton;
  private final JFrame parentFrame;

  public UserInfoPanel(JFrame parent) {
    this.parentFrame = parent;
    this.setLayout(null);
    userLabel = new JLabel();
    authButton = new JButton();
    userLabel.setFont(new Font("Arial", Font.PLAIN, 14));
    authButton.setFont(new Font("Arial", Font.PLAIN, 12));
    userLabel.setBounds(0, 0, 120, 30);
    authButton.setBounds(125, 0, 60, 30);
    this.add(userLabel);
    this.add(authButton);
    updateDisplay();

    authButton.addActionListener(e -> {
      if (UserManager.getInstance().isRegisteredUser()) {
        UserManager.getInstance().logout();
        parentFrame.setTitle("连连看 - 游客");
        updateDisplay();
      } else {
        // 打开登录窗口并关闭当前游戏窗口（登录流程会新开 GameFrame）
        new LoginFrame();
        parentFrame.dispose();
      }
    });
  }

  /** 根据当前登录状态更新显示 */
  public void updateDisplay() {
    if (UserManager.getInstance().isRegisteredUser()) {
      userLabel.setText("User: " + UserManager.getInstance().getCurrentUser());
      authButton.setText("登出");
    } else {
      userLabel.setText("User: 游客");
      authButton.setText("登录");
    }
    repaint();
  }
}
