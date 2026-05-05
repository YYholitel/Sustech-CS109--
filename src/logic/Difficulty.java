package logic;

//标记了数量
public enum Difficulty {
  Easy(5),
  Hard(12);

  private final int maxIconType;

  // 设置难度 模块
  Difficulty(int maxIconType) {
    this.maxIconType = maxIconType;
  }

  public int getMaxIconType() {
    return maxIconType;
  }
}