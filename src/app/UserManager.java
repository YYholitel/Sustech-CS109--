package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理器：负责注册、登录、战绩管理
 * 数据持久化到 users.json
 */
public class UserManager {
    private static final String USER_FILE = "users.json";
    private static UserManager instance;
    private Map<String, UserData> users;
    private String currentUser = null; // null 表示游客

    public static class UserData {
        public String username;
        public String passwordHash; // SHA-256 哈希
        public int totalGames;
        public int totalWins;
        public int highestScore;

        public UserData() {}

        public UserData(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.totalGames = 0;
            this.totalWins = 0;
            this.highestScore = 0;
        }
    }

    private UserManager() {
        users = new HashMap<>();
        loadUsers();
    }

    public static UserManager getInstance() {
        if (instance == null) {
            instance = new UserManager();
        }
        return instance;
    }

    /**
     * 从文件加载用户数据
     */
    private void loadUsers() {
        File file = new File(USER_FILE);
        if (!file.exists()) {
            return;
        }
        try {
            String json = new String(Files.readAllBytes(Paths.get(USER_FILE)), StandardCharsets.UTF_8);
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, UserData>>(){}.getType();
            Map<String, UserData> loaded = gson.fromJson(json, mapType);
            if (loaded != null) {
                users = loaded;
            }
        } catch (IOException e) {
            System.err.println("加载用户文件失败: " + e.getMessage());
        }
    }

    /**
     * 保存用户数据到文件
     */
    private void saveUsers() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(users);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(USER_FILE), StandardCharsets.UTF_8))) {
            writer.write(json);
        } catch (IOException e) {
            System.err.println("保存用户文件失败: " + e.getMessage());
        }
    }

    /**
     * 对密码进行 SHA-256 哈希
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
        }
    }

    /**
     * 注册新用户
     * @return null 表示注册成功，否则返回错误信息
     */
    public String register(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "用户名不能为空";
        }
        if (password == null || password.trim().isEmpty()) {
            return "密码不能为空";
        }
        username = username.trim();
        if (users.containsKey(username)) {
            return "用户名已存在";
        }
        if (username.length() < 3 || username.length() > 20) {
            return "用户名长度需在3-20个字符之间";
        }
        if (password.length() < 4) {
            return "密码长度不能少于4个字符";
        }
        UserData userData = new UserData(username, hashPassword(password));
        users.put(username, userData);
        saveUsers();
        return null; // 注册成功
    }

    /**
     * 登录
     * @return null 表示登录成功，否则返回错误信息
     */
    public String login(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            return "请输入用户名";
        }
        if (password == null || password.trim().isEmpty()) {
            return "请输入密码";
        }
        username = username.trim();
        UserData userData = users.get(username);
        if (userData == null) {
            return "用户不存在";
        }
        if (!userData.passwordHash.equals(hashPassword(password))) {
            return "密码错误";
        }
        currentUser = username;
        return null; // 登录成功
    }

    /**
     * 以游客身份登录
     */
    public void loginAsGuest() {
        currentUser = null;
    }

    /**
     * 登出
     */
    public void logout() {
        currentUser = null;
    }

    /**
     * 获取当前登录用户名，null表示游客
     */
    public String getCurrentUser() {
        return currentUser;
    }

    /**
     * 判断是否为注册用户（非游客）
     */
    public boolean isRegisteredUser() {
        return currentUser != null;
    }

    /**
     * 获取当前用户数据
     */
    public UserData getCurrentUserData() {
        if (currentUser == null) return null;
        return users.get(currentUser);
    }

    /**
     * 更新用户战绩
     */
    public void updateScore(int score, boolean isWin) {
        if (currentUser == null) return;
        UserData userData = users.get(currentUser);
        if (userData == null) return;
        userData.totalGames++;
        if (isWin) {
            userData.totalWins++;
        }
        if (score > userData.highestScore) {
            userData.highestScore = score;
        }
        saveUsers();
    }
}