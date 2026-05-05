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
    int totalPairs = empties.size() / 2;
    int requiredTurnPairs = (int) Math.ceil(totalPairs * 0.6);
    java.util.Random random = new java.util.Random();
    return backtrackFill(gameBoard, board, empties, 0, 0, totalPairs, requiredTurnPairs, random);
  }

  private boolean backtrackFill(GameBoard gameBoard, Cell[][] board, java.util.List<Position> empties,
      int pairIndex, int turnPairs, int totalPairs, int requiredTurnPairs, java.util.Random random) {
    if (empties.isEmpty()) {
      return turnPairs >= requiredTurnPairs;
    }

    int remainingPairs = empties.size() / 2;
    if (turnPairs + remainingPairs < requiredTurnPairs) {
      return false;
    }

    java.util.List<PairCandidate> candidates = buildCandidates(gameBoard, empties);
    if (candidates.isEmpty()) {
      return false;
    }
    java.util.Collections.shuffle(candidates, random);

    int maxIconType = difficulty.getMaxIconType();
    int type = (pairIndex % maxIconType) + 1;

    for (PairCandidate candidate : candidates) {
      Position posA = empties.get(candidate.idxA);
      Position posB = empties.get(candidate.idxB);

      board[posA.getRow()][posA.getCol()] = new Cell(posA, false, type);
      board[posB.getRow()][posB.getCol()] = new Cell(posB, false, type);

      int idxA = candidate.idxA;
      int idxB = candidate.idxB;
      if (idxA > idxB) {
        int temp = idxA;
        idxA = idxB;
        idxB = temp;
      }
      empties.remove(idxB);
      empties.remove(idxA);

      int nextTurnPairs = turnPairs + (candidate.turns > 0 ? 1 : 0);
      if (backtrackFill(gameBoard, board, empties, pairIndex + 1, nextTurnPairs, totalPairs,
          requiredTurnPairs, random)) {
        return true;
      }

      empties.add(idxA, posA);
      empties.add(idxB, posB);
      board[posA.getRow()][posA.getCol()] = new Cell(posA, true, 0);
      board[posB.getRow()][posB.getCol()] = new Cell(posB, true, 0);
    }

    return false;
  }

  private java.util.List<PairCandidate> buildCandidates(GameBoard gameBoard, java.util.List<Position> empties) {
    java.util.List<PairCandidate> candidates = new java.util.ArrayList<>();
    for (int i = 0; i < empties.size(); i++) {
      Position posA = empties.get(i);
      for (int j = i + 1; j < empties.size(); j++) {
        Position posB = empties.get(j);
        java.util.List<Position> path = Utils.findLinkPath(gameBoard, posA, posB);
        if (path != null) {
          int turns = countTurns(path);
          candidates.add(new PairCandidate(i, j, turns));
        }
      }
    }
    return candidates;
  }

  private int countTurns(java.util.List<Position> path) {
    if (path.size() < 3) {
      return 0;
    }
    int turns = 0;
    int prevDr = path.get(1).getRow() - path.get(0).getRow();
    int prevDc = path.get(1).getCol() - path.get(0).getCol();
    for (int i = 2; i < path.size(); i++) {
      int dr = path.get(i).getRow() - path.get(i - 1).getRow();
      int dc = path.get(i).getCol() - path.get(i - 1).getCol();
      if ((dr != 0 && prevDr == 0) || (dc != 0 && prevDc == 0)) {
        turns++;
      }
      prevDr = dr;
      prevDc = dc;
    }
    return turns;
  }

  private static class PairCandidate {
    final int idxA;
    final int idxB;
    final int turns;

    PairCandidate(int idxA, int idxB, int turns) {
      this.idxA = idxA;
      this.idxB = idxB;
      this.turns = turns;
    }
  }
}
