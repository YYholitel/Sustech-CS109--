package app;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class SaveManager {
    private static final String SAVE_DIR = "saves";

    public static class SaveData {
        public String username;
        public int slotIndex;
        public long timestamp;
        public int rows;
        public int cols;
        public int[][] boardIcons;
        public boolean[][] boardEmpty;
        public int score;
        public int seconds;
        public int minutes;
        public int hours;
        public String difficulty;
        public int remainingPairs;

        public SaveData() {
            timestamp = System.currentTimeMillis();
        }
    }

    private static File getSaveDir() {
        File dir = new File(SAVE_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    private static String getSaveFilePath(String username, int slotIndex) {
        return SAVE_DIR + File.separator + username + "_slot" + slotIndex + ".json";
    }

    public static String save(SaveData data) {
        if (data.username == null || data.username.trim().isEmpty()) {
            return "游客无法存档，请先登录";
        }
        if (data.slotIndex < 0 || data.slotIndex > 2) {
            return "无效的存档槽位";
        }
        getSaveDir();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(data);
        String filePath = getSaveFilePath(data.username, data.slotIndex);
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8))) {
            writer.write(json);
            return null;
        } catch (IOException e) {
            return "保存失败: " + e.getMessage();
        }
    }

    public static SaveData load(String username, int slotIndex) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String filePath = getSaveFilePath(username, slotIndex);
        File file = new File(filePath);
        if (!file.exists()) {
            return null;
        }
        try {
            String json = new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

            // 先用 JsonParser 手动解析，避免二维数组反序列化问题
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();

            SaveData data = new SaveData();
            data.username = root.get("username").getAsString();
            data.slotIndex = root.get("slotIndex").getAsInt();
            data.timestamp = root.get("timestamp").getAsLong();
            data.rows = root.get("rows").getAsInt();
            data.cols = root.get("cols").getAsInt();
            data.score = root.get("score").getAsInt();
            data.seconds = root.get("seconds").getAsInt();
            data.minutes = root.get("minutes").getAsInt();
            data.hours = root.get("hours").getAsInt();
            data.difficulty = root.get("difficulty").getAsString();
            data.remainingPairs = root.get("remainingPairs").getAsInt();

            // 手动解析 boardIcons
            JsonArray iconsArray = root.getAsJsonArray("boardIcons");
            data.boardIcons = new int[data.rows][data.cols];
            for (int i = 0; i < data.rows && i < iconsArray.size(); i++) {
                JsonArray row = iconsArray.get(i).getAsJsonArray();
                for (int j = 0; j < data.cols && j < row.size(); j++) {
                    data.boardIcons[i][j] = row.get(j).getAsInt();
                }
            }

            // 手动解析 boardEmpty
            JsonArray emptyArray = root.getAsJsonArray("boardEmpty");
            data.boardEmpty = new boolean[data.rows][data.cols];
            for (int i = 0; i < data.rows && i < emptyArray.size(); i++) {
                JsonArray row = emptyArray.get(i).getAsJsonArray();
                for (int j = 0; j < data.cols && j < row.size(); j++) {
                    data.boardEmpty[i][j] = row.get(j).getAsBoolean();
                }
            }

            return data;

        } catch (Exception e) {
            System.err.println("读取存档失败: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static SaveSlotInfo[] getSaveSlots(String username) {
        SaveSlotInfo[] slots = new SaveSlotInfo[3];
        for (int i = 0; i < 3; i++) {
            SaveData data = load(username, i);
            if (data != null) {
                slots[i] = new SaveSlotInfo(i, data.timestamp, data.score, data.difficulty, data.remainingPairs);
            }
        }
        return slots;
    }

    public static boolean deleteSave(String username, int slotIndex) {
        String filePath = getSaveFilePath(username, slotIndex);
        File file = new File(filePath);
        return file.exists() && file.delete();
    }

    public static class SaveSlotInfo {
        public int slotIndex;
        public long timestamp;
        public int score;
        public String difficulty;
        public int remainingPairs;
        public boolean isEmpty;

        public SaveSlotInfo(int slotIndex, long timestamp, int score, String difficulty, int remainingPairs) {
            this.slotIndex = slotIndex;
            this.timestamp = timestamp;
            this.score = score;
            this.difficulty = difficulty;
            this.remainingPairs = remainingPairs;
            this.isEmpty = false;
        }

        public SaveSlotInfo(int slotIndex) {
            this.slotIndex = slotIndex;
            this.isEmpty = true;
        }
    }
}