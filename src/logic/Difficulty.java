package logic;

/**
 * 难度枚举：定义不同难度下的图标种类上限和总计时秒数
 */
public enum Difficulty {
  Level1(5, 240, true),
  Level2(6, 220, false),
  Level3(8, 200, false),
  Level4(9, 180, false),
  Level5(10, 180, false),
  Level6(12, 150, false);

  private final int maxIconType;
  private final int totalSeconds;
  private boolean unlocked;

  // 设置难度 模块
  Difficulty(int maxIconType, int totalSeconds, boolean unlocked) {
    this.maxIconType = maxIconType;
    this.totalSeconds = totalSeconds;
    this.unlocked = unlocked;
  }

  public int getMaxIconType() {
    int num = maxIconType;
    return num;

  }

  public int getTotalSeconds() {
    return totalSeconds;
  }

  public boolean isUnlocked() {
    return unlocked;
  }

  public void setUnlocked(boolean unlocked) {
    this.unlocked = unlocked;
  }

  public static void unlockNextLevel(Difficulty clearedLevel) {
    int next = clearedLevel.ordinal() + 1;
    Difficulty[] all = values();
    if (next < all.length) {
      all[next].setUnlocked(true);
    }
  }
}