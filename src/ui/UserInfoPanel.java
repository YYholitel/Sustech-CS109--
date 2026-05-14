package ui;

import app.LoginFrame;
import app.UserManager;

import javax.swing.*;
import java.awt.*;

/**
 * 顶部用户信息面板：显示当前用户名并提供登录/登出按钮
 */
public class UserInfoPanel extends JPanel {
  private static final UiLayoutScaler LAYOUT_SCALER = new UiLayoutScaler(185, 30);
  private final JLabel userLabel;
  private final ModernButton authButton;
  private final JFrame parentFrame;

  public UserInfoPanel(JFrame parent) {
    this.parentFrame = parent;
    this.setLayout(null);
    userLabel = new JLabel();
    authButton = new ModernButton("");
    userLabel.setFont(UiFont.font(Font.PLAIN, 14));
    authButton.setFont(UiFont.font(Font.PLAIN, 12));
    this.add(userLabel);
    this.add(authButton);
    updateLayout(185, 30);
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

  public void updateLayout(int width, int height) {
    double scale = LAYOUT_SCALER.getScaleFactor(Math.max(1, width), Math.max(1, height));
    int labelWidth = LAYOUT_SCALER.scale(120, scale);
    int buttonWidth = LAYOUT_SCALER.scale(60, scale);
    int buttonHeight = LAYOUT_SCALER.scale(30, scale);
    int buttonX = LAYOUT_SCALER.scale(125, scale);
    userLabel.setBounds(0, 0, labelWidth, buttonHeight);
    authButton.setBounds(buttonX, 0, buttonWidth, buttonHeight);
    setPreferredSize(new Dimension(LAYOUT_SCALER.scale(185, scale), buttonHeight));
    repaint();
  }
}
