package logic;

import model.Cell;
import model.GameBoard;
import model.Position;
import utils.Utils;

public class CellGenerator {
  // 根据难度生成棋盘
  private Difficulty difficulty;

  public CellGenerator(Difficulty difficulty) {
    this.difficulty = difficulty;
  }

  public void setDifficulty(Difficulty difficulty) {
    this.difficulty = difficulty;
  }

  public Cell randomCell(Position pos) {
    int type = (int) (Math.random() * difficulty.getMaxIconType()) + 1;
    return new Cell(pos, false, type);
  }

  // 生成棋盘（保证每种图标成对出现）
  public GameBoard generateBoard(int size) {
    int total = size + 2;
    java.util.List<Position> fillPositions = getFillPositions(size);
    GameBoard result = null;
    int maxRetries = 60;
    for (int attempt = 0; attempt < maxRetries; attempt++) {
      Cell[][] board = new Cell[total][total];
      // 先初始化所有位置为空格
      for (int i = 0; i < total; i++) {
        for (int j = 0; j < total; j++) {
          board[i][j] = new Cell(new Position(i, j), true, 0);
        }
      }

      GameBoard gameBoard = new GameBoard(total, total, board);
      if (fillByReverse(gameBoard, board, fillPositions)) {
        return gameBoard;
      }
      result = gameBoard;
    }
    return result;
  }

  private java.util.List<Position> getFillPositions(int size) {
    java.util.List<Position> positions = new java.util.ArrayList<>();
    if (difficulty == Difficulty.Easy) {
      int blockSize = 4;
      int minSize = blockSize * 2 + 1;
      if (size >= minSize) {
        int start2 = size - blockSize;
        addBlock(positions, 1, 1, blockSize);
        addBlock(positions, start2 + 1, start2 + 1, blockSize);
        return positions;
      }
    }

    for (int i = 1; i < size + 1; i++) {
      for (int j = 1; j < size + 1; j++) {
        positions.add(new Position(i, j));
      }
    }
    return positions;
  }

  private void addBlock(java.util.List<Position> positions, int startRow, int startCol, int blockSize) {
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        positions.add(new Position(startRow + i, startCol + j));
      }
    }
  }

  private boolean fillByReverse(GameBoard gameBoard, Cell[][] board, java.util.List<Position> fillPositions) {
    java.util.List<Position> empties = new java.util.ArrayList<>(fillPositions);
    java.util.Random random = new java.util.Random();
    int maxIconType = difficulty.getMaxIconType();
    int pairIndex = 0;

    while (empties.size() >= 2) {
      boolean placed = false;
      int tryLimit = Math.min(200, empties.size() * empties.size());
      for (int t = 0; t < tryLimit; t++) {
        int idxA = random.nextInt(empties.size());
        int idxB = random.nextInt(empties.size() - 1);
        if (idxB >= idxA) {
          idxB += 1;
        }
        Position posA = empties.get(idxA);
        Position posB = empties.get(idxB);
        if (Utils.canLinkAB(gameBoard, posA, posB)) {
          int type = (pairIndex % maxIconType) + 1;
          board[posA.getRow()][posA.getCol()] = new Cell(posA, false, type);
          board[posB.getRow()][posB.getCol()] = new Cell(posB, false, type);
          if (idxA > idxB) {
            empties.remove(idxA);
            empties.remove(idxB);
          } else {
            empties.remove(idxB);
            empties.remove(idxA);
          }
          pairIndex++;
          placed = true;
          break;
        }
      }
      if (!placed) {
        int[] indices = findFirstConnectablePairIndices(gameBoard, empties);
        if (indices != null) {
          int idxA = indices[0];
          int idxB = indices[1];
          Position posA = empties.get(idxA);
          Position posB = empties.get(idxB);
          int type = (pairIndex % maxIconType) + 1;
          board[posA.getRow()][posA.getCol()] = new Cell(posA, false, type);
          board[posB.getRow()][posB.getCol()] = new Cell(posB, false, type);
          if (idxA > idxB) {
            empties.remove(idxA);
            empties.remove(idxB);
          } else {
            empties.remove(idxB);
            empties.remove(idxA);
          }
          pairIndex++;
          placed = true;
        }
      }
      if (!placed) {
        return false;
      }
    }
    return empties.isEmpty();
  }

  private int[] findFirstConnectablePairIndices(GameBoard gameBoard, java.util.List<Position> empties) {
    for (int i = 0; i < empties.size(); i++) {
      Position posA = empties.get(i);
      for (int j = i + 1; j < empties.size(); j++) {
        Position posB = empties.get(j);
        if (Utils.canLinkAB(gameBoard, posA, posB)) {
          return new int[] { i, j };
        }
      }
    }
    return null;
  }
}
