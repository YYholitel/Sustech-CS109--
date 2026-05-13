package logic;

/**
 * 难度枚举：定义不同难度下的图标种类上限和总计时秒数
 */
public enum Difficulty {
  Easy(5, 120),
  Hard(12, 150);

  private final  int maxIconType;
  private final int totalSeconds;

  // 设置难度 模块
  Difficulty(int maxIconType, int totalSeconds) {
    this.maxIconType = maxIconType;
    this.totalSeconds = totalSeconds;
  }

  public  int getMaxIconType() { int num = maxIconType;
      return num;

  }

  public int getTotalSeconds() {
    return totalSeconds;
  }
}