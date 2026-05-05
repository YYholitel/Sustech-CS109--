package ui;

import model.*;
import model.Rectangle;
import utils.Utils;

import javax.swing.*;
import java.awt.*;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoardPanel extends JPanel {
    int offSetX;
    int offSetY;

    List<Image> imageList = new ArrayList<>();
    GameBoard gameBoard;

    List<Line> lineList = new ArrayList<>();
    int totalRow;
    int totalCol;
    boolean lineVisible;
    int width;
    int height;
    int cellWidth;
    int cellHeight;
    Position firstSelected = null;
    Position secondSelected = null;
    boolean animating = false;
    Scores scores;
    StatusPanel statusPanel;
    Runnable onBoardUpdated;
    boolean gameOver = false;
    int recordCount = 0;
    UndoSnapshot lastUndo = null;

    private static class UndoSnapshot {
        private final Position posA;
        private final Position posB;
        private final int iconA;
        private final int iconB;
        private final int scoreBefore;
        private final int comboBefore;

        private UndoSnapshot(Position posA, Position posB, int iconA, int iconB, int scoreBefore, int comboBefore) {
            this.posA = posA;
            this.posB = posB;
            this.iconA = iconA;
            this.iconB = iconB;
            this.scoreBefore = scoreBefore;
            this.comboBefore = comboBefore;
        }
    }

    public Position getPositionByPoint(int x, int y) {

        int col = x / cellWidth;
        int row = y / cellHeight;
        if (row < 0 || row >= totalRow || col < 0 || col >= totalCol) {
            return null;
        }
        return new Position(row, col);
    }

    public boolean isAdjacent(Position p1, Position p2) {
        // 修改
        int dr = Math.abs(p1.getRow() - p2.getRow());
        int dc = Math.abs(p1.getCol() - p2.getCol());
        return dr + dc == 1;
    }

    public void showLine(Position p1, Position p2) {
        lineList.clear();
        lineList.add(new Line(p1, p2));
        lineVisible = true;
        repaint();
    }

    public void showLinkPath(List<Position> path) {
        lineList.clear();
        for (int i = 0; i < path.size() - 1; i++) {
            lineList.add(new Line(path.get(i), path.get(i + 1)));
        }
        lineVisible = true;
        repaint();
    }

    public void clearLine() {
        lineVisible = false;
        lineList.clear();
        repaint();
    }

    public BoardPanel(GameBoard gameBoard, int offSetX, int offSetY, int width, int height, Scores scores,
            StatusPanel statusPanel, Runnable onBoardUpdated) {
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.setBounds(offSetX, offSetY, width, height);
        this.totalRow = gameBoard.getRowCnt();
        this.totalCol = gameBoard.getColCnt();
        this.width = width;
        this.height = height;
        this.setLayout(new GridLayout(this.totalRow, this.totalCol));
        this.gameBoard = gameBoard;
        this.setPreferredSize(new Dimension(this.width, this.height));
        this.cellWidth = this.width / totalCol;
        this.cellHeight = this.height / totalRow;
        this.scores = scores;
        this.statusPanel = statusPanel;
        this.onBoardUpdated = onBoardUpdated;
        resetTracking();
        File dir = new File("resource");
        File[] files = dir.listFiles();

        // 加载图片，如果存在0.jpg或0.png，优先加载它作为空白图像，并确保它在imageList的第一个位置 4.29 21.13

        if (files != null) {
            File zeroJpg = null;
            File zeroPng = null;
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String name = file.getName().toLowerCase();
                if (name.equals("0.jpg")) {
                    zeroJpg = file;
                } else if (name.equals("0.png")) {
                    zeroPng = file;
                }
            }
            File zeroFile = (zeroJpg != null) ? zeroJpg : zeroPng;
            if (zeroFile != null) {
                imageList.add(new ImageIcon(zeroFile.getPath()).getImage());
            }

            java.util.Arrays.sort(files, (a, b) -> a.getName().compareToIgnoreCase(b.getName()));
            for (File file : files) {
                if (!file.isFile()) {
                    continue;
                }
                String name = file.getName().toLowerCase();
                if (name.equals("0.jpg") || name.equals("0.png")) {
                    continue;
                }
                if (name.endsWith(".png") || name.endsWith(".jpg")) {
                    ImageIcon icon = new ImageIcon(file.getPath());
                    imageList.add(icon.getImage());
                }
            }
        }

        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleClick(e.getX(), e.getY());
            }
        });
    }

    // 设置棋盘，每次设置新的棋盘时重置选中状态和连线状态
    public void setGameBoard(GameBoard gameBoard) {
        this.gameBoard = gameBoard;
        this.totalRow = gameBoard.getRowCnt();
        this.totalCol = gameBoard.getColCnt();
        this.cellWidth = this.width / totalCol;
        this.cellHeight = this.height / totalRow;
        this.firstSelected = null;
        this.secondSelected = null;
        this.lineVisible = false;
        this.lineList.clear();
        this.animating = false;
        this.gameOver = false;
        resetTracking();
        repaint();
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
        if (gameOver) {
            gameBoard.clearAllChosen();
            firstSelected = null;
            secondSelected = null;
            lastUndo = null;
            clearLine();
        }
    }

    public GameBoard getGameBoard() {
        return gameBoard;
    }

    public void resetTracking() {
        recordCount = 0;
        lastUndo = null;
        clearRecordFile();
    }

    public boolean undoLastClear() {
        if (gameOver || animating || lastUndo == null) {
            return false;
        }
        Cell c1 = gameBoard.getCell(lastUndo.posA.getRow(), lastUndo.posA.getCol());
        Cell c2 = gameBoard.getCell(lastUndo.posB.getRow(), lastUndo.posB.getCol());
        c1.restore(lastUndo.iconA);
        c2.restore(lastUndo.iconB);
        gameBoard.clearAllChosen();
        firstSelected = null;
        secondSelected = null;
        clearLine();
        if (scores != null) {
            scores.restoreState(lastUndo.scoreBefore, lastUndo.comboBefore);
        }
        lastUndo = null;
        repaint();
        if (onBoardUpdated != null) {
            onBoardUpdated.run();
        }
        return true;
    }

    public void handleClick(int x, int y) {
        if (gameOver) {
            return;
        }
        if (animating) {
            return;
        }

        Position pos = getPositionByPoint(x, y);
        if (pos == null) {
            return;
        }

        Cell clickedCell = gameBoard.getCell(pos.getRow(), pos.getCol());
        if (clickedCell == null || clickedCell.isEmpty()) {
            return;
        }

        if (firstSelected == null) {
            gameBoard.clearAllChosen();
            clickedCell.setChosen(true);
            firstSelected = pos;
            repaint();
            return;
        }

        if (firstSelected.equals(pos)) {
            clickedCell.setChosen(false);
            firstSelected = null;
            secondSelected = null;
            repaint();
            return;
        }

        secondSelected = pos;
        Cell secondCell = gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol());

        secondCell.setChosen(true);
        repaint();

        Cell firstCell = gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol());

        // 判断消除条件：图标相同 且 可连线
        if (isTheSame(firstCell, secondCell) && Utils.canLinkAB(gameBoard, firstSelected, secondSelected)) {
            List<Position> linkPath = Utils.findLinkPath(gameBoard, firstSelected, secondSelected);
            int scoreBefore = scores != null ? scores.getScore() : 0;
            int comboBefore = scores != null ? scores.getCombo() : 0;
            lastUndo = new UndoSnapshot(firstSelected, secondSelected,
                    firstCell.getIconIndex(), secondCell.getIconIndex(),
                    scoreBefore, comboBefore);
            writeClearRecord(firstCell, secondCell, linkPath);
            animating = true;
            showLinkPath(linkPath);
            Timer timer = new Timer(300, e -> {
                Cell c1 = gameBoard.getCell(firstSelected.getRow(), firstSelected.getCol());
                Cell c2 = gameBoard.getCell(secondSelected.getRow(), secondSelected.getCol());
                c1.setEmpty(true);
                c2.setEmpty(true);
                c1.setChosen(false);
                c2.setChosen(false);
                lineVisible = false;
                lineList.clear();
                if (scores != null) {
                    scores.onPairCleared();
                }
                firstSelected = null;
                secondSelected = null;
                animating = false;
                repaint();
                if (onBoardUpdated != null) {
                    onBoardUpdated.run();
                }
            });
            timer.setRepeats(false);
            timer.start();
        } else {
            gameBoard.clearAllChosen();
            firstSelected = null;
            secondSelected = null;
            lastUndo = null;
            if (scores != null) {
                scores.resetCombo();
            }
            repaint();
        }
    }

    private void writeClearRecord(Cell firstCell, Cell secondCell, List<Position> linkPath) {
        if (recordCount >= 3) {
            return;
        }
        File file = new File("resource/records/Records.txt");
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        String pathText = formatPath(linkPath);
        String line = "Step " + (recordCount + 1)
                + ": icons=" + firstCell.getIconIndex() + "," + secondCell.getIconIndex()
                + "; posA=(" + firstSelected.getRow() + "," + firstSelected.getCol() + ")"
                + "; posB=(" + secondSelected.getRow() + "," + secondSelected.getCol() + ")"
                + "; path=" + pathText;
        try (FileWriter writer = new FileWriter(file, true)) {
            writer.write(line + System.lineSeparator());
            recordCount++;
        } catch (IOException ex) {
            recordCount++;
        }
    }

    private String formatPath(List<Position> path) {
        if (path == null || path.isEmpty()) {
            return "n/a";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < path.size(); i++) {
            Position pos = path.get(i);
            if (i > 0) {
                sb.append("->");
            }
            sb.append("(").append(pos.getRow()).append(",").append(pos.getCol()).append(")");
        }
        return sb.toString();
    }

    private void clearRecordFile() {
        File file = new File("resource/records/Records.txt");
        File parent = file.getParentFile();
        if (parent != null) {
            parent.mkdirs();
        }
        try (FileWriter writer = new FileWriter(file, false)) {
            writer.write("");
        } catch (IOException ex) {
            return;
        }
    }

    // 4.29 修改
    public boolean isTheSame(Cell c1, Cell c2) {
        return c1.getIconIndex() == c2.getIconIndex();
    }

    public Rectangle getRectangle(Position position) {
        int x = position.getCol() * cellWidth;
        int y = position.getRow() * cellHeight;
        return new Rectangle(x, y, cellWidth, cellHeight);
    }

    private Point getCenterPoint(Position position) {
        int x, y;
        // 越界位置：将坐标限制在棋盘边缘，中心点贴边
        int row = position.getRow();
        int col = position.getCol();
        if (row < 0) {
            y = -cellHeight / 2;
        } else if (row >= totalRow) {
            y = totalRow * cellHeight + cellHeight / 2;
        } else {
            y = row * cellHeight + cellHeight / 2;
        }
        if (col < 0) {
            x = -cellWidth / 2;
        } else if (col >= totalCol) {
            x = totalCol * cellWidth + cellWidth / 2;
        } else {
            x = col * cellWidth + cellWidth / 2;
        }
        return new Point(x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        for (int i = 0; i < gameBoard.getRowCnt(); i++) {
            for (int j = 0; j < gameBoard.getColCnt(); j++) {
                Rectangle rec = getRectangle(new Position(i, j));
                g2.drawImage(
                        imageList.get(gameBoard.getCell(i, j).getIconIndex()),
                        rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(),
                        this);
                if (gameBoard.getCell(i, j).getIsChosen()) {
                    g2.setColor(Color.RED);
                    g2.setStroke(new BasicStroke(3));
                    g2.drawRect(
                            rec.getX() + 1,
                            rec.getY() + 1,
                            rec.getWidth() - 3,
                            rec.getHeight() - 3);
                } else {
                    g2.setColor(Color.GRAY);
                    g2.setStroke(new BasicStroke(1));
                    g2.drawRect(
                            rec.getX(),
                            rec.getY(),
                            rec.getWidth() - 1,
                            rec.getHeight() - 1);
                }
            }
        }
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));
        if (lineVisible) {
            for (Line line : lineList) {
                Point p1 = getCenterPoint(line.getStart());
                Point p2 = getCenterPoint(line.getEnd());
                g2.drawLine(p1.x, p1.y, p2.x, p2.y);
            }
        }

    }
}
