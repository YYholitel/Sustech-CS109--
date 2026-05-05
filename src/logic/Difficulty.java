package logic;

//标记了数量
public enum Difficulty {
  Easy(5, 120),
  Hard(12, 150);

  private final int maxIconType;
  private final int totalSeconds;

  // 设置难度 模块
  Difficulty(int maxIconType, int totalSeconds) {
    this.maxIconType = maxIconType;
    this.totalSeconds = totalSeconds;
  }

  public int getMaxIconType() {
    return maxIconType;
  }

  public int getTotalSeconds() {
    return totalSeconds;
  }
}