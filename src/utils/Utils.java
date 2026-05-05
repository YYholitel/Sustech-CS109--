package utils;

import model.Cell;
import model.GameBoard;
import model.Position;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static boolean hasNonEmptyCell(GameBoard gameBoard) {
        for (int r = 0; r < gameBoard.getRowCnt(); r++) {
            for (int c = 0; c < gameBoard.getColCnt(); c++) {
                if (!gameBoard.getCell(r, c).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasAnyValidMove(GameBoard gameBoard) {
        int rowCnt = gameBoard.getRowCnt();
        int colCnt = gameBoard.getColCnt();
        for (int r1 = 0; r1 < rowCnt; r1++) {
            for (int c1 = 0; c1 < colCnt; c1++) {
                Cell cellA = gameBoard.getCell(r1, c1);
                if (cellA.isEmpty()) {
                    continue;
                }
                int icon = cellA.getIconIndex();
                for (int r2 = r1; r2 < rowCnt; r2++) {
                    int startC = (r2 == r1) ? c1 + 1 : 0;
                    for (int c2 = startC; c2 < colCnt; c2++) {
                        Cell cellB = gameBoard.getCell(r2, c2);
                        if (cellB.isEmpty() || cellB.getIconIndex() != icon) {
                            continue;
                        }
                        if (canLinkAB(gameBoard, cellA.getPos(), cellB.getPos())) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isEmptyAt(GameBoard gameBoard, int row, int col) {
        if (row < 0 || row >= gameBoard.getRowCnt() || col < 0 || col >= gameBoard.getColCnt()) {
            return true;
        }
        return gameBoard.getCell(row, col).isEmpty();
    }

    private static boolean isClearStraight(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getRow() == posB.getRow()) {
            int start = posA.getCol();
            int end = posB.getCol();
            int step = (start < end) ? 1 : -1;
            for (int c = start + step; c != end; c += step) {
                if (!isEmptyAt(gameBoard, posA.getRow(), c)) {
                    return false;
                }
            }
            return true;
        }
        if (posA.getCol() == posB.getCol()) {
            int start = posA.getRow();
            int end = posB.getRow();
            int step = (start < end) ? 1 : -1;
            for (int r = start + step; r != end; r += step) {
                if (!isEmptyAt(gameBoard, r, posA.getCol())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public static List<Cell> getReachablePointsInFourDirections(GameBoard gameBoard, Position posA) {
        List<Cell> res = new ArrayList<>();
        for (int i = posA.getRow() + 1; i < gameBoard.getRowCnt(); i++) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        for (int i = posA.getRow() - 1; i >= 0; i--) {
            if (gameBoard.getCell(i, posA.getCol()).isEmpty()) {
                res.add(gameBoard.getCell(i, posA.getCol()));
            } else {
                break;
            }
        }
        for (int i = posA.getCol() + 1; i < gameBoard.getColCnt(); i++) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        for (int i = posA.getCol() - 1; i >= 0; i--) {
            if (gameBoard.getCell(posA.getRow(), i).isEmpty()) {
                res.add(gameBoard.getCell(posA.getRow(), i));
            } else {
                break;
            }
        }
        return res;
    }

    private static List<Position> findZeroTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        if (isClearStraight(gameBoard, posA, posB)) {
            List<Position> path = new ArrayList<>();
            path.add(posA);
            path.add(posB);
            return path;
        }
        return null;
    }

    private static List<Position> findOneTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        if (posA.getCol() == posB.getCol() || posA.getRow() == posB.getRow()) {
            return null;
        }

        Position cornerPoint1 = new Position(posA.getRow(), posB.getCol());
        Position cornerPoint2 = new Position(posB.getRow(), posA.getCol());

        if ((isEmptyAt(gameBoard, cornerPoint1.getRow(), cornerPoint1.getCol()) || cornerPoint1.equals(posA)
                || cornerPoint1.equals(posB))
                && isClearStraight(gameBoard, posA, cornerPoint1)
                && isClearStraight(gameBoard, cornerPoint1, posB)) {
            List<Position> path = new ArrayList<>();
            path.add(posA);
            path.add(cornerPoint1);
            path.add(posB);
            return path;
        }

        if ((isEmptyAt(gameBoard, cornerPoint2.getRow(), cornerPoint2.getCol()) || cornerPoint2.equals(posA)
                || cornerPoint2.equals(posB))
                && isClearStraight(gameBoard, posA, cornerPoint2)
                && isClearStraight(gameBoard, cornerPoint2, posB)) {
            List<Position> path = new ArrayList<>();
            path.add(posA);
            path.add(cornerPoint2);
            path.add(posB);
            return path;
        }

        return null;
    }

    private static List<Position> getReachablePointsWithOutside(GameBoard gameBoard, Position posA) {
        List<Position> res = new ArrayList<>();
        int rowCnt = gameBoard.getRowCnt();
        int colCnt = gameBoard.getColCnt();

        // 向下：扫描到棋盘边界外
        int r = posA.getRow() + 1;
        while (r < rowCnt && gameBoard.getCell(r, posA.getCol()).isEmpty()) {
            res.add(new Position(r, posA.getCol()));
            r++;
        }
        if (r >= rowCnt) {
            res.add(new Position(rowCnt, posA.getCol()));
        }

        // 向上：扫描到棋盘边界外
        r = posA.getRow() - 1;
        while (r >= 0 && gameBoard.getCell(r, posA.getCol()).isEmpty()) {
            res.add(new Position(r, posA.getCol()));
            r--;
        }
        if (r < 0) {
            res.add(new Position(-1, posA.getCol()));
        }

        // 向右：扫描到棋盘边界外
        int c = posA.getCol() + 1;
        while (c < colCnt && gameBoard.getCell(posA.getRow(), c).isEmpty()) {
            res.add(new Position(posA.getRow(), c));
            c++;
        }
        if (c >= colCnt) {
            res.add(new Position(posA.getRow(), colCnt));
        }

        // 向左：扫描到棋盘边界外
        c = posA.getCol() - 1;
        while (c >= 0 && gameBoard.getCell(posA.getRow(), c).isEmpty()) {
            res.add(new Position(posA.getRow(), c));
            c--;
        }
        if (c < 0) {
            res.add(new Position(posA.getRow(), -1));
        }

        return res;
    }

    private static List<Position> findTwoTurnPath(GameBoard gameBoard, Position posA, Position posB) {
        List<Position> reachablePoints = getReachablePointsWithOutside(gameBoard, posA);
        for (Position pivot : reachablePoints) {
            List<Position> oneTurn = findOneTurnPath(gameBoard, pivot, posB);
            if (oneTurn != null) {
                List<Position> path = new ArrayList<>();
                path.add(posA);
                path.add(pivot);
                for (int i = 1; i < oneTurn.size(); i++) {
                    path.add(oneTurn.get(i));
                }
                return path;
            }
        }
        return null;
    }

    public static List<Position> findLinkPath(GameBoard gameBoard, Position posA, Position posB) {
        List<Position> path = findZeroTurnPath(gameBoard, posA, posB);
        if (path != null) {
            return path;
        }
        path = findOneTurnPath(gameBoard, posA, posB);
        if (path != null) {
            return path;
        }
        return findTwoTurnPath(gameBoard, posA, posB);
    }

    public static boolean canLinkAB(GameBoard gameBoard, Position posA, Position posB) {
        return findLinkPath(gameBoard, posA, posB) != null;
    }
}
