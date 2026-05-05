package app;

import ui.GameFrame;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import javax.swing.*;
import java.nio.file.Paths;
import java.util.List;

public class Main2 {
    public static List<String> usernames;

    public static void main (String [] args){
        String filename = "resource/records/Records.txt";
        Path filepath = Paths.get(filename);
        try {
            usernames = Files.readAllLines(filepath);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        
        SwingUtilities.invokeLater(()->{
            GameFrame frame = new GameFrame("连连看", 800, 1000);
            frame.repaint();
            frame.setVisible(false);

            JFrame loginFrame  = new JFrame("登录");
            loginFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            loginFrame.setSize(400, 200);
            loginFrame.setLayout(null);

            JLabel promptLabel = new JLabel("请输入用户名：");
            promptLabel.setSize(120, 30);
            promptLabel.setLocation(50, 20);

            JTextField textField = new JTextField();
            textField.setSize(100, 50);
            textField.setLocation(50,50);

            JButton login = new JButton("登录");
            login.setLocation(50,100);
            login.setSize(100,30);
            login.addActionListener(e -> {
                boolean match = false;

                for (String l : usernames) {
                    if (textField.getText().equals(l)) {
                        match = true;
                    }
                }

                if (match) {
                    JOptionPane.showMessageDialog(loginFrame, "欢迎！");
                    frame.setVisible(true);
                    loginFrame.dispose();
                } else {
                    JOptionPane.showMessageDialog(loginFrame, "用户名不存在，请重试！", "登录失败", JOptionPane.ERROR_MESSAGE);
                }
            });

            loginFrame.add(login);
            loginFrame.add(promptLabel);
            loginFrame.add(textField);
            loginFrame.setVisible(true);

    });

}
}
