package logic;

import model.Cell;
import model.GameBoard;
import model.Position;
import utils.Utils;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
      if (filling(gameBoard, board, fillPositions)) {
        return gameBoard;
      }
      result = gameBoard;
    }
    return result;
  }

  private java.util.List<Position> getFillPositions(int size) {
    java.util.List<Position> positions = new java.util.ArrayList<>();

    // Level4~Level6 固定使用原 Hard 模式：整盘填充
    if (difficulty == Difficulty.Level4 || difficulty == Difficulty.Level5 || difficulty == Difficulty.Level6) {
      fillAllInnerCells(positions, size);
      return positions;
    }

    // Level1~Level3 与 Level1 统一：使用两个 4x4 方块的填充方式
    if (difficulty == Difficulty.Level1 || difficulty == Difficulty.Level2 || difficulty == Difficulty.Level3) {
      int blockSize = 4;
      int minSize = blockSize * 2 + 1;
      if (size >= minSize) {
        int start2 = size - blockSize;
        addBlock(positions, 1, 1, blockSize);
        addBlock(positions, start2 + 1, start2 + 1, blockSize);
        return positions;
      }
    }

    fillAllInnerCells(positions, size);
    return positions;
  }

  private void fillAllInnerCells(java.util.List<Position> positions, int size) {
    for (int i = 1; i < size + 1; i++) {
      for (int j = 1; j < size + 1; j++) {
        positions.add(new Position(i, j));
      }
    }
  }

  private void addBlock(java.util.List<Position> positions, int startRow, int startCol, int blockSize) {
    for (int i = 0; i < blockSize; i++) {
      for (int j = 0; j < blockSize; j++) {
        positions.add(new Position(startRow + i, startCol + j));
      }
    }
  }

  private boolean filling(GameBoard gameBoard, Cell[][] board, java.util.List<Position> fillPositions) {
    int maxIconType = difficulty.getMaxIconType();
    int total = fillPositions.size();

    // 生成成对的图标列表
    List<Integer> icons = new ArrayList<>();
    for (int i = 0; i < total / 2; i++) {
      int type = i % maxIconType + 1;
      icons.add(type);
      icons.add(type);
    }

    // 随机打乱
    Collections.shuffle(icons);

    // 填充到棋盘
    for (int i = 0; i < fillPositions.size(); i++) {
      Position pos = fillPositions.get(i);
      int type = icons.get(i);
      board[pos.getRow()][pos.getCol()] = new Cell(pos, false, type);
    }

    return true;
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
