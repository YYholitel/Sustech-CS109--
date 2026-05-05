package ui;

import javax.swing.*;
import java.awt.*;

public class StatusPanel extends JPanel {
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
        statusLabel.setFont(new Font("Arial", Font.BOLD, 50));
        timeLabel.setFont(new Font("Arial", Font.BOLD, 50));
        Dimension size = statusLabel.getPreferredSize();
        Dimension timeLabelSize = timeLabel.getPreferredSize();
        int x = (width - size.width) / 6;
        int y = (height - size.height) / 3;
        int time_x = (width - timeLabelSize.width) * 5 / 6;
        int time_y = (height - timeLabelSize.height) / 3;
        statusLabel.setBounds(x, y, size.width, size.height);
        timeLabel.setBounds(time_x, time_y, timeLabelSize.width, timeLabelSize.height);
        int scoresWidth = width / 3;
        int scoresHeight = height - 20;
        scores.setBounds((width - scoresWidth) / 2, 10, scoresWidth, scoresHeight);
        this.add(statusLabel);
        this.add(scores);
        this.add(timeLabel);
    }

    public void setStatus(String text) {
        statusLabel.setText(text);
        Dimension size = statusLabel.getPreferredSize();
        int x = (width - size.width) / 6;
        int y = (height - size.height) / 3;
        statusLabel.setBounds(x, y, size.width, size.height);
        repaint();
    }

    public Scores getScores() {
        return scores;
    }

    public void startCountdown(int totalSeconds) {
        stopTimer();
        remainingSeconds = Math.max(0, totalSeconds);
        updateTimeLabel();
        timer.start();
    }

    public void stopTimer() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    public void resetTimer() {
        stopTimer();
        remainingSeconds = 0;
        updateTimeLabel();
    }

    public int getRemainingSeconds() {
        return remainingSeconds;
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
