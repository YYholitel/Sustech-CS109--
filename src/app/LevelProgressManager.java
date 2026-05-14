package app;

import logic.Difficulty;

/**
 * 关卡进度管理：
 * 1) 登录用户：进度持久化到 users.json
 * 2) 游客：仅内存记录，应用退出后丢失
 */
public final class LevelProgressManager {
    private static int guestMaxUnlockedLevel = 1;

    private LevelProgressManager() {
    }

    public static void syncToCurrentPlayer() {
        int maxUnlocked = getCurrentMaxUnlockedLevel();
        applyUnlockedByMaxLevel(maxUnlocked);
    }

    public static void onLevelCleared(Difficulty clearedLevel) {
        int currentMax = getCurrentMaxUnlockedLevel();
        int nextUnlock = Math.min(Difficulty.values().length, clearedLevel.ordinal() + 2);
        int newMax = Math.max(currentMax, nextUnlock);

        UserManager userManager = UserManager.getInstance();
        if (userManager.isRegisteredUser()) {
            userManager.updateCurrentUserMaxUnlockedLevel(newMax);
        } else {
            guestMaxUnlockedLevel = newMax;
        }
        applyUnlockedByMaxLevel(newMax);
    }

    private static int getCurrentMaxUnlockedLevel() {
        UserManager userManager = UserManager.getInstance();
        if (userManager.isRegisteredUser()) {
            return userManager.getCurrentUserMaxUnlockedLevel();
        }
        return Math.max(1, guestMaxUnlockedLevel);
    }

    private static void applyUnlockedByMaxLevel(int maxUnlockedLevel) {
        Difficulty[] levels = Difficulty.values();
        for (int i = 0; i < levels.length; i++) {
            levels[i].setUnlocked(i + 1 <= maxUnlockedLevel);
        }
    }
}
