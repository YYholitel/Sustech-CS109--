package model;

public class GameBoard {
    int rowCnt;
    int colCnt;
    Cell[][] board;

    public GameBoard(int rowCnt, int colCnt, Cell[][] border) {
        this.rowCnt = rowCnt;
        this.colCnt = colCnt;
        this.board = border;
    }

    public int getRowCnt() {
        return rowCnt;
    }

    public int getColCnt() {
        return colCnt;
    }
    public Cell getCell(int row, int col) {
        return board[row][col];
    }
    public void clearAllChosen() {
        for (int i = 0; i < rowCnt; i++) {
            for (int j = 0; j < colCnt; j++) {
                board[i][j].setChosen(false);
            }
        }
    }
}
