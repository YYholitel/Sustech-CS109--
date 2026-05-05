package app;
import ui.GameFrame;
import javax.swing.*;
import java.awt.*;

/**
 * 登录/注册界面
 */
public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JButton guestButton;
    private JLabel messageLabel;

    public LoginFrame() {
        super("连连看 - 登录");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 350);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel panel = new JPanel();
        panel.setLayout(null);
        panel.setBackground(new Color(240, 240, 240));

        // 标题
        JLabel titleLabel = new JLabel("连连看");
        titleLabel.setFont(new Font("微软雅黑", Font.BOLD, 28));
        titleLabel.setBounds(130, 20, 200, 40);
        panel.add(titleLabel);

        // 用户名标签
        JLabel userLabel = new JLabel("用户名:");
        userLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        userLabel.setBounds(60, 80, 80, 25);
        panel.add(userLabel);

        // 用户名输入框
        usernameField = new JTextField();
        usernameField.setBounds(140, 80, 180, 30);
        usernameField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(usernameField);

        // 密码标签
        JLabel passLabel = new JLabel("密码:");
        passLabel.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        passLabel.setBounds(60, 125, 80, 25);
        panel.add(passLabel);

        // 密码输入框
        passwordField = new JPasswordField();
        passwordField.setBounds(140, 125, 180, 30);
        passwordField.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        panel.add(passwordField);

        // 消息标签
        messageLabel = new JLabel("");
        messageLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
        messageLabel.setForeground(Color.RED);
        messageLabel.setBounds(60, 160, 280, 25);
        panel.add(messageLabel);

        // 登录按钮
        loginButton = new JButton("登录");
        loginButton.setBounds(60, 200, 120, 35);
        loginButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        loginButton.setFocusPainted(false);
        loginButton.addActionListener(e -> handleLogin());
        panel.add(loginButton);

        // 注册按钮
        registerButton = new JButton("注册");
        registerButton.setBounds(200, 200, 120, 35);
        registerButton.setFont(new Font("微软雅黑", Font.BOLD, 14));
        registerButton.setFocusPainted(false);
        registerButton.addActionListener(e -> handleRegister());
        panel.add(registerButton);

        // 游客模式按钮
        guestButton = new JButton("游客模式");
        guestButton.setBounds(100, 250, 200, 35);
        guestButton.setFont(new Font("微软雅黑", Font.PLAIN, 14));
        guestButton.setFocusPainted(false);
        guestButton.addActionListener(e -> handleGuest());
        panel.add(guestButton);

        // 回车键登录
        getRootPane().setDefaultButton(loginButton);

        add(panel);
        setVisible(true);
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