package app;

import ui.GameFrame;
import ui.ModernButton;
import ui.UiFont;
import javax.swing.*;
import java.awt.*;

/**
 * 登录/注册界面
 */
public class LoginFrame extends JFrame {
    private final JPanel panel;
    private final JLabel titleLabel;
    private final JLabel userLabel;
    private final JLabel passLabel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private ModernButton loginButton;
    private ModernButton registerButton;
    private ModernButton guestButton;
    private JLabel messageLabel;
    private final ui.UiLayoutScaler layoutScaler = new ui.UiLayoutScaler(400, 350);

    public LoginFrame() {
        super("连连看 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(360, 320));
        setResizable(true);

        panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(240, 240, 240));

        // 标题
        titleLabel = new JLabel("连连看");
        titleLabel.setFont(UiFont.font(Font.BOLD, 28));
        panel.add(titleLabel);

        // 用户名标签
        userLabel = new JLabel("用户名:");
        userLabel.setFont(UiFont.font(Font.PLAIN, 14));
        panel.add(userLabel);

        // 用户名输入框
        usernameField = new JTextField();
        usernameField.setFont(UiFont.font(Font.PLAIN, 14));
        panel.add(usernameField);

        // 密码标签
        passLabel = new JLabel("密码:");
        passLabel.setFont(UiFont.font(Font.PLAIN, 14));
        panel.add(passLabel);

        // 密码输入框
        passwordField = new JPasswordField();
        passwordField.setFont(UiFont.font(Font.PLAIN, 14));
        panel.add(passwordField);

        // 消息标签
        messageLabel = new JLabel("");
        messageLabel.setFont(UiFont.font(Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        panel.add(messageLabel);

        // 登录按钮
        loginButton = new ModernButton("登录");
        loginButton.setFont(UiFont.font(Font.BOLD, 14));
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton);

        // 注册按钮
        registerButton = new ModernButton("注册");
        registerButton.setFont(UiFont.font(Font.BOLD, 14));
        registerButton.addActionListener(e -> handleRegister());
        panel.add(registerButton);

        // 游客模式按钮
        guestButton = new ModernButton("游客模式");
        guestButton.setFont(UiFont.font(Font.PLAIN, 14));
        guestButton.addActionListener(e -> handleGuest());
        panel.add(guestButton);

        // 回车键登录
        getRootPane().setDefaultButton(loginButton);

        add(panel);
        layoutForm();
        addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                layoutForm();
            }
        });
        setVisible(true);
    }

    private void layoutForm() {
        int currentWidth = panel.getWidth();
        int currentHeight = panel.getHeight();
        if (currentWidth <= 0) {
            currentWidth = getWidth();
        }
        if (currentHeight <= 0) {
            currentHeight = getHeight();
        }
        currentWidth = Math.max(1, currentWidth);
        currentHeight = Math.max(1, currentHeight);
        double scale = layoutScaler.getScaleFactor(currentWidth, currentHeight);

        titleLabel.setBounds(layoutScaler.scaleRect(130, 20, 200, 40, scale));
        userLabel.setBounds(layoutScaler.scaleRect(60, 80, 80, 25, scale));
        usernameField.setBounds(layoutScaler.scaleRect(140, 80, 180, 30, scale));
        passLabel.setBounds(layoutScaler.scaleRect(60, 125, 80, 25, scale));
        passwordField.setBounds(layoutScaler.scaleRect(140, 125, 180, 30, scale));
        messageLabel.setBounds(layoutScaler.scaleRect(60, 160, 280, 25, scale));
        loginButton.setBounds(layoutScaler.scaleRect(60, 200, 120, 35, scale));
        registerButton.setBounds(layoutScaler.scaleRect(200, 200, 120, 35, scale));
        guestButton.setBounds(layoutScaler.scaleRect(100, 250, 200, 35, scale));
    }

    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        String error = UserManager.getInstance().login(username, password);
        if (error != null) {
            messageLabel.setText(error);
        } else {
            messageLabel.setText("");
            JOptionPane.showMessageDialog(this, "登录成功！欢迎 " + username);
            dispose();
            launchGame();
        }
    }

    private void handleRegister() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        String error = UserManager.getInstance().register(username, password);
        if (error != null) {
            messageLabel.setText(error);
        } else {
            messageLabel.setText("注册成功！请登录");
            // 注册成功后自动登录
            UserManager.getInstance().login(username, password);
            JOptionPane.showMessageDialog(this, "注册成功！欢迎 " + username);
            dispose();
            launchGame();
        }
    }

    private void handleGuest() {
        UserManager.getInstance().loginAsGuest();
        dispose();
        launchGame();
    }

    private void launchGame() {
        String title = "连连看";
        if (UserManager.getInstance().isRegisteredUser()) {
            title += " - " + UserManager.getInstance().getCurrentUser();
        } else {
            title += " - 游客";
        }
        GameFrame frame = new GameFrame(title, 800, 1000);
        frame.repaint();
    }
}