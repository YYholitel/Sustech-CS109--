package ui;

import model.*;
import model.Rectangle;
import ui.AppConfig;
import ui.IconTheme;
import utils.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

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
    private final Timer comboPopupTimer;
    private String comboPopupText = null;
    private Color comboPopupColor = new Color(76, 175, 80);
    private float comboPopupAlpha = 0f;
    private long comboPopupShownAt = 0L;
    private static final int COMBO_POPUP_DURATION_MS = 1200;
    private static final int COMBO_POPUP_FADE_START_MS = 750;
    private static final int COMBO_POPUP_TICK_MS = 40;
    private final Font comboPopupFont = new Font("Serif", Font.BOLD | Font.ITALIC, 54);

    // 简易淡出+缩放动画（最小实现）
    private Map<String, Long> fadeMap = new HashMap<>(); // key -> startTime
    private List<Position[]> fadingPairs = new ArrayList<>();
    private Timer animationTimer = null;
    private static final int FADE_ANIM_TICK_MS = 16; // 更平滑 (~60 FPS)
    private static final int FADE_DURATION_MS = 400; // 延长淡出时长

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
        IconTheme selectedTheme = AppConfig.getSelectedIconTheme();
        File dir = selectedTheme == null ? new File("resource/pictures/animals") : selectedTheme.getDirectory();
        if (!dir.exists()) {
            dir = new File("resource/pictures/animals");
        }
        File[] files = dir.listFiles();

        imageList.add(createBlankImage());

        // 加载图片，如果存在0.jpg或0.png，优先加载它作为空白图像，并确保它在imageList的第一个位置 4.29 21.13

        if (files != null) {
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

        comboPopupTimer = new Timer(COMBO_POPUP_TICK_MS, e -> updateComboPopup());
        comboPopupTimer.setRepeats(true);
    }

    public void setBoardBounds(int x, int y, int width, int height) {
        this.offSetX = x;
        this.offSetY = y;
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        setBounds(x, y, this.width, this.height);
        setPreferredSize(new Dimension(this.width, this.height));
        this.cellWidth = Math.max(1, this.width / Math.max(1, totalCol));
        this.cellHeight = Math.max(1, this.height / Math.max(1, totalRow));
        repaint();
    }

    private Image createBlankImage() {
        BufferedImage blank = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        return blank;
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
        hideComboPopup();
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
            hideComboPopup();
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

            // 启动简易淡出+缩放动画（非阻塞）
            startFadeAnimationForPair(firstSelected, secondSelected);
        } else {
            gameBoard.clearAllChosen();
            secondCell.setChosen(true);
            firstSelected = secondSelected;

            secondSelected = null;
            lastUndo = null;
            if (scores != null) {
                scores.resetCombo();
            }
            hideComboPopup();
            repaint();
        }
    }

    private void showComboPopup(int combo) {
        if (combo <= 0) {
            hideComboPopup();
            return;
        }
        comboPopupText = "COMBO x" + combo;
        comboPopupColor = getComboPopupColor(combo);
        comboPopupAlpha = 1f;
        comboPopupShownAt = System.currentTimeMillis();
        comboPopupTimer.restart();
        repaint();
    }

    private void hideComboPopup() {
        comboPopupTimer.stop();
        comboPopupText = null;
        comboPopupAlpha = 0f;
        comboPopupShownAt = 0L;
    }

    private void updateComboPopup() {
        if (comboPopupText == null) {
            comboPopupTimer.stop();
            return;
        }
        long elapsed = System.currentTimeMillis() - comboPopupShownAt;
        if (elapsed >= COMBO_POPUP_DURATION_MS) {
            hideComboPopup();
            repaint();
            return;
        }
        if (elapsed >= COMBO_POPUP_FADE_START_MS) {
            float fadeProgress = (elapsed - COMBO_POPUP_FADE_START_MS)
                    / (float) (COMBO_POPUP_DURATION_MS - COMBO_POPUP_FADE_START_MS);
            comboPopupAlpha = Math.max(0f, 1f - fadeProgress);
        } else {
            comboPopupAlpha = 1f;
        }
        repaint();
    }

    private Color getComboPopupColor(int combo) {
        if (combo <= 1) {
            return new Color(76, 175, 80);
        }
        if (combo == 2) {
            return new Color(144, 238, 144);
        }
        if (combo == 3) {
            return new Color(255, 248, 196);
        }
        if (combo == 4) {
            return new Color(255, 235, 59);
        }
        if (combo == 5) {
            return new Color(255, 167, 38);
        }
        if ( combo == 6){
            return new Color (255,60,43);
        }
        if (combo == 7){
            return new Color (255,0,0);
        }
        return null;
    }


    private void paintComboPopup(Graphics2D g2) {
        if (comboPopupText == null || comboPopupAlpha <= 0f) {
            return;
        }
        Graphics2D popup = (Graphics2D) g2.create();
        popup.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        popup.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        popup.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        popup.setFont(comboPopupFont);
        FontMetrics metrics = popup.getFontMetrics();
        int textWidth = metrics.stringWidth(comboPopupText);
        int textHeight = metrics.getHeight();
        int ascent = metrics.getAscent();
        int x = (getWidth() - textWidth) / 2;
        int y = (getHeight() - textHeight) / 2 + ascent;
        Composite originalComposite = popup.getComposite();

        popup.setComposite(AlphaComposite.SrcOver.derive(comboPopupAlpha * 0.18f));
        popup.setColor(comboPopupColor);
        popup.drawString(comboPopupText, x - 4, y - 4);
        popup.drawString(comboPopupText, x + 4, y - 4);
        popup.drawString(comboPopupText, x - 4, y + 4);
        popup.drawString(comboPopupText, x + 4, y + 4);

        popup.setComposite(AlphaComposite.SrcOver.derive(comboPopupAlpha * 0.35f));
        popup.setColor(Color.BLACK);
        popup.drawString(comboPopupText, x + 3, y + 5);

        popup.setComposite(AlphaComposite.SrcOver.derive(comboPopupAlpha));
        popup.setColor(comboPopupColor);
        popup.drawString(comboPopupText, x, y);

        popup.setComposite(originalComposite);
        popup.dispose();
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
        this.width = Math.max(1, getWidth());
        this.height = Math.max(1, getHeight());
        this.cellWidth = Math.max(1, this.width / Math.max(1, totalCol));
        this.cellHeight = Math.max(1, this.height / Math.max(1, totalRow));
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < gameBoard.getRowCnt(); i++) {
            for (int j = 0; j < gameBoard.getColCnt(); j++) {
                Position pos = new Position(i, j);
                Rectangle rec = getRectangle(pos);

                // 跳过正在消除的棋子（动画期间）
                // 淡出动画已禁用
                if (false) {
                    // 淡出动画代码已注释
                    continue; // 跳过边框绘制
                }

                // 直接绘制棋子（不再做开局隐身/掉落显隐）
                String fadeKey = posKey(pos);
                if (fadeMap.containsKey(fadeKey)) {
                    long start = fadeMap.get(fadeKey);
                    float prog = Math.min(1f, (System.currentTimeMillis() - start) / (float) FADE_DURATION_MS);
                    float eased = easeOutQuad(prog);
                    float alpha = Math.max(0f, 1f - eased);
                    float scale = 1f - eased * 0.35f; // 缩小到约65%

                    Composite orig = g2.getComposite();
                    g2.setComposite(AlphaComposite.SrcOver.derive(alpha));

                    int scaledW = (int) (rec.getWidth() * scale);
                    int scaledH = (int) (rec.getHeight() * scale);
                    int offX = rec.getX() + (rec.getWidth() - scaledW) / 2;
                    int offY = rec.getY() + (rec.getHeight() - scaledH) / 2;
                    g2.drawImage(
                            imageList.get(gameBoard.getCell(i, j).getIconIndex()),
                            offX, offY, scaledW, scaledH,
                            this);

                    g2.setComposite(orig);
                } else {
                    g2.drawImage(
                            imageList.get(gameBoard.getCell(i, j).getIconIndex()),
                            rec.getX(), rec.getY(), rec.getWidth(), rec.getHeight(),
                            this);
                }

                // 绘制边框
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

        // 绘制连线（带动画效果）
        g2.setColor(Color.RED);
        g2.setStroke(new BasicStroke(3));
        if (lineVisible) {
            paintLinesWithAnimation(g2);
        }

        paintComboPopup(g2);
    }

    /**
     * 绘制带动画效果的连线
     */
    private void paintLinesWithAnimation(Graphics2D g2) {
        if (lineList.isEmpty()) {
            return;
        }

        for (int i = 0; i < lineList.size(); i++) {
            Line line = lineList.get(i);
            Point p1 = getCenterPoint(line.getStart());
            Point p2 = getCenterPoint(line.getEnd());

            // 计算当前线段的显示比例
            float segmentRatio = 1.0f; // 简化：直接显示所有线

            if (segmentRatio > 0) {
                // 如果线段未完全显示，画部分线
                if (segmentRatio < 1.0f) {
                    int endX = (int) (p1.x + (p2.x - p1.x) * segmentRatio);
                    int endY = (int) (p1.y + (p2.y - p1.y) * segmentRatio);
                    g2.drawLine(p1.x, p1.y, endX, endY);
                } else {
                    // 完全显示这条线
                    g2.drawLine(p1.x, p1.y, p2.x, p2.y);
                }
            }
        }
    }

    public void startDroppingAnimation() {
        // 兼容调用方：不再执行开局隐身/掉落动画，直接刷新
        repaint();
    }

    // 简易动画 - 帮助方法
    private String posKey(Position p) {
        return p.getRow() + "_" + p.getCol();
    }

    private void startFadeAnimationForPair(Position a, Position b) {
        long now = System.currentTimeMillis();
        fadeMap.put(posKey(a), now);
        fadeMap.put(posKey(b), now);
        fadingPairs.add(new Position[] { a, b });

        if (animationTimer == null) {
            animationTimer = new Timer(FADE_ANIM_TICK_MS, ev -> {
                long t = System.currentTimeMillis();
                // 检查完成的配对
                Iterator<Position[]> it = fadingPairs.iterator();
                List<Position[]> finished = new ArrayList<>();
                while (it.hasNext()) {
                    Position[] pair = it.next();
                    String k1 = posKey(pair[0]);
                    String k2 = posKey(pair[1]);
                    Long s1 = fadeMap.get(k1);
                    Long s2 = fadeMap.get(k2);
                    if (s1 == null || s2 == null)
                        continue;
                    if (t - s1 >= FADE_DURATION_MS && t - s2 >= FADE_DURATION_MS) {
                        finished.add(pair);
                    }
                }
                for (Position[] pair : finished) {
                    // 真正移除棋子
                    Cell c1 = gameBoard.getCell(pair[0].getRow(), pair[0].getCol());
                    Cell c2 = gameBoard.getCell(pair[1].getRow(), pair[1].getCol());
                    if (c1 != null) {
                        c1.setEmpty(true);
                        c1.setChosen(false);
                    }
                    if (c2 != null) {
                        c2.setEmpty(true);
                        c2.setChosen(false);
                    }
                    fadeMap.remove(posKey(pair[0]));
                    fadeMap.remove(posKey(pair[1]));
                    fadingPairs.remove(pair);
                    lineVisible = false;
                    lineList.clear();
                    if (scores != null) {
                        scores.onPairCleared();
                        showComboPopup(scores.getCombo());
                    }
                    firstSelected = null;
                    secondSelected = null;
                    animating = false;
                    if (onBoardUpdated != null) {
                        onBoardUpdated.run();
                    }
                }
                repaint();
                // 停止计时器当没有活动动画
                if (fadingPairs.isEmpty()) {
                    ((Timer) ev.getSource()).stop();
                    animationTimer = null;
                }
            });
            animationTimer.setRepeats(true);
            animationTimer.start();
        }
    }

    // 简单缓动函数：缓出
    private static float easeOutQuad(float p) {
        return 1 - (1 - p) * (1 - p);
    }

    /**
     * 检查是否正在进行掉落动画
     */
    public boolean isAnimatingDrop() {
        return false;
    }

    /**
     * 检查棋子是否可见
     */
    public boolean areCellsVisible() {
        return true;
    }

}
