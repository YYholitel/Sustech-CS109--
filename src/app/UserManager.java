package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
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

    /**
     * 成绩记录类
     */
    public static class GameRecord {
        public String difficulty;  // 难度名称
        public int seconds;        // 用时（秒）
        public int score;          // 得分
        public long timestamp;     // 完成时间戳

        public GameRecord() {}  // GSON 需要空构造函数

        public GameRecord(String difficulty, int seconds, int score) {
            this.difficulty = difficulty;
            this.seconds = seconds;
            this.score = score;
            this.timestamp = System.currentTimeMillis();
        }
    }

    /**
     * 带用户名的成绩记录（用于排行榜）
     */
    public static class GameRecordWithUser {
        public String username;
        public GameRecord record;

        public GameRecordWithUser(String username, GameRecord record) {
            this.username = username;
            this.record = record;
        }
    }

    public static class UserData {
        public String username;
        public String passwordHash; // SHA-256 哈希
        public int totalGames;
        public int totalWins;
        public int highestScore;
        public int maxUnlockedLevel;
        public List<GameRecord> records;  // 历史成绩

        public UserData() {}

        public UserData(String username, String passwordHash) {
            this.username = username;
            this.passwordHash = passwordHash;
            this.totalGames = 0;
            this.totalWins = 0;
            this.highestScore = 0;
            this.maxUnlockedLevel = 1;
            this.records = new ArrayList<>();
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
                for (UserData userData : users.values()) {
                    if (userData.maxUnlockedLevel <= 0) {
                        userData.maxUnlockedLevel = 1;
                    }
                    if (userData.records == null) {
                        userData.records = new ArrayList<>();
                    }
                }
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

    /**
     * 添加游戏成绩记录
     */
    public void addGameRecord(String difficulty, int seconds, int score) {
        if (currentUser == null) return;
        UserData userData = users.get(currentUser);
        if (userData == null) return;
        if (userData.records == null) {
            userData.records = new ArrayList<>();
        }
        userData.records.add(new GameRecord(difficulty, seconds, score));
        saveUsers();
    }

    /**
     * 获取所有用户的成绩（用于排行榜）
     * 按难度优先级（高难度在前）和用时（短在前）排序
     */
    public List<GameRecordWithUser> getAllRecordsSorted() {
        List<GameRecordWithUser> allRecords = new ArrayList<>();

        // 难度优先级映射（Level6 最高优先级）
        Map<String, Integer> difficultyPriority = new HashMap<>();
        difficultyPriority.put("Level1", 1);
        difficultyPriority.put("Level2", 2);
        difficultyPriority.put("Level3", 3);
        difficultyPriority.put("Level4", 4);
        difficultyPriority.put("Level5", 5);
        difficultyPriority.put("Level6", 6);

        for (Map.Entry<String, UserData> entry : users.entrySet()) {
            String username = entry.getKey();
            UserData data = entry.getValue();
            if (data.records != null) {
                for (GameRecord record : data.records) {
                    allRecords.add(new GameRecordWithUser(username, record));
                }
            }
        }

        // 排序：难度优先（高难度在前），然后用时（短在前）
        allRecords.sort((a, b) -> {
            int prioA = difficultyPriority.getOrDefault(a.record.difficulty, 0);
            int prioB = difficultyPriority.getOrDefault(b.record.difficulty, 0);
            if (prioA != prioB) {
                return Integer.compare(prioB, prioA);  // 高难度在前
            }
            return Integer.compare(a.record.seconds, b.record.seconds);  // 用时短在前
        });

        return allRecords;
    }

    public int getCurrentUserMaxUnlockedLevel() {
        if (currentUser == null) {
            return 1;
        }
        UserData userData = users.get(currentUser);
        if (userData == null) {
            return 1;
        }
        return Math.max(1, userData.maxUnlockedLevel);
    }

    public void updateCurrentUserMaxUnlockedLevel(int maxUnlockedLevel) {
        if (currentUser == null) {
            return;
        }
        UserData userData = users.get(currentUser);
        if (userData == null) {
            return;
        }
        int normalized = Math.max(1, maxUnlockedLevel);
        if (normalized > userData.maxUnlockedLevel) {
            userData.maxUnlockedLevel = normalized;
            saveUsers();
        }
    }
}