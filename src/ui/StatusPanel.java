package ui;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
    private static final UiLayoutScaler LAYOUT_SCALER = new UiLayoutScaler(800, 100);
    JLabel statusLabel;
    JLabel timeLabel;
    Scores scores;
    Timer timer;
    int remainingSeconds;
    Runnable onTimeUp;
    int offSetX;
    int offSetY;
    int width;
    int height;
    private boolean started = false;
    private boolean paused = false;

    public StatusPanel(int offSetX, int offSetY, int width, int height) {
        this.setLayout(null);
        this.setBounds(offSetX, offSetY, width, height);
        this.offSetX = offSetX;
        this.offSetY = offSetY;
        this.width = width;
        this.height = height;
        statusLabel = new JLabel("ready");
        timeLabel = new JLabel("00:00:00");
        scores = new Scores();
        timer = new Timer(1000, e -> {
            if (remainingSeconds <= 0) {
                stopTimer();
                return;
            }
            remainingSeconds--;
            updateTimeLabel();
            if (remainingSeconds <= 0) {
                stopTimer();
                if (onTimeUp != null) {
                    onTimeUp.run();
                }
            }
        });
        statusLabel.setFont(UiFont.font(Font.BOLD, 50));
        timeLabel.setFont(UiFont.font(Font.BOLD, 50));
        this.add(statusLabel);
        this.add(scores);
        this.add(timeLabel);
        updateLayout(width, height);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
        updateLayout(width, height);
    }

    public void updateLayout(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        double scale = LAYOUT_SCALER.getScaleFactor(this.width, this.height);
        int topBarHeight = LAYOUT_SCALER.scale(36, scale);
        int contentY = topBarHeight;
        int contentHeight = Math.max(1, this.height - topBarHeight);

        Dimension size = statusLabel.getPreferredSize();
        Dimension timeLabelSize = timeLabel.getPreferredSize();
        int x = (this.width - size.width) / 6;
        int y = contentY + Math.max(0, (contentHeight - size.height) / 2);
        int timeX = (this.width - timeLabelSize.width) * 5 / 6;
        int timeY = contentY + Math.max(0, (contentHeight - timeLabelSize.height) / 2);
        statusLabel.setBounds(x, y, size.width, size.height);
        timeLabel.setBounds(timeX, timeY, timeLabelSize.width, timeLabelSize.height);

        int scoresWidth = LAYOUT_SCALER.scale(240, scale);
        int scoresHeight = Math.max(1, contentHeight - LAYOUT_SCALER.scale(6, scale));
        scores.setBounds((this.width - scoresWidth) / 2, contentY + LAYOUT_SCALER.scale(3, scale), scoresWidth,
                scoresHeight);
        scores.repaint();
        repaint();
    }

    public Scores getScores() {
        return scores;
    }

    public void startCountdown(int totalSeconds) {
        stopTimer();
        remainingSeconds = Math.max(0, totalSeconds);
        started = true;
        paused = false;
        updateTimeLabel();
        timer.start();
    }

    public void stopTimer() {
        if (timer.isRunning()) {
            timer.stop();
        }
        paused = false;
    }

    public void pauseCountdown() {
        if (!started || remainingSeconds <= 0 || paused) {
            return;
        }
        if (timer.isRunning()) {
            timer.stop();
        }
        paused = true;
    }

    public void resumeCountdown() {
        if (!started || remainingSeconds <= 0 || !paused) {
            return;
        }
        timer.start();
        paused = false;
    }

    public void resetTimer() {
        stopTimer();
        remainingSeconds = 0;
        // 保留 started 状态：一旦用户开始过游戏，视为已开始，不再将其视为第一次
        updateTimeLabel();
    }

    public boolean hasStarted() {
        return started;
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setOnTimeUp(Runnable onTimeUp) {
        this.onTimeUp = onTimeUp;
    }

    private void updateTimeLabel() {
        int hours = remainingSeconds / 3600;
        int minutes = (remainingSeconds % 3600) / 60;
        int seconds = remainingSeconds % 60;
        timeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }
}
