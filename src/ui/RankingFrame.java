package ui;

import app.UserManager;
import app.UserManager.GameRecordWithUser;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * 用户成绩排行榜
 * 按难度优先级（高难度在前）和用时（短在前）排序
 */
public class RankingFrame extends JFrame {
    private final JTable rankingTable;
    private final DefaultTableModel tableModel;

    // 难度显示名称映射
    private static final String[] DIFFICULTY_NAMES = {
            "Level1", "Level2", "Level3", "Level4", "Level5", "Level6"
    };
    private static final String[] DISPLAY_NAMES = {
            "第1关", "第2关", "第3关", "第4关", "第5关", "第6关"
    };

    public RankingFrame() {
        super("连连看 - 排行榜");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(800, 550);
        setMinimumSize(new Dimension(650, 400));
        setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(new Color(245, 247, 250));

        // 标题区域
        JPanel titlePanel = new JPanel(new BorderLayout());
        titlePanel.setBackground(new Color(245, 247, 250));
        titlePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));

        JLabel titleLabel = new JLabel("🏆 成绩排行榜", SwingConstants.CENTER);
        titleLabel.setFont(UiFont.font(Font.BOLD, 28));
        titleLabel.setForeground(new Color(41, 128, 185));
        titlePanel.add(titleLabel, BorderLayout.CENTER);

        JLabel subLabel = new JLabel("按难度（高→低）和用时（短→长）排序", SwingConstants.CENTER);
        subLabel.setFont(UiFont.font(Font.PLAIN, 12));
        subLabel.setForeground(Color.GRAY);
        titlePanel.add(subLabel, BorderLayout.SOUTH);

        mainPanel.add(titlePanel, BorderLayout.NORTH);

        // 表格模型
        String[] columns = {"排名", "玩家", "难度", "用时", "得分", "完成时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        rankingTable = new JTable(tableModel);
        rankingTable.setFont(UiFont.font(Font.PLAIN, 14));
        rankingTable.setRowHeight(32);
        rankingTable.setShowGrid(true);
        rankingTable.setGridColor(new Color(220, 224, 230));

        // 表头样式
        JTableHeader header = rankingTable.getTableHeader();
        header.setFont(UiFont.font(Font.BOLD, 14));
        header.setBackground(new Color(52, 152, 219));
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 35));

        // 设置列宽
        rankingTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        rankingTable.getColumnModel().getColumn(1).setPreferredWidth(120);
        rankingTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        rankingTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        rankingTable.getColumnModel().getColumn(4).setPreferredWidth(80);
        rankingTable.getColumnModel().getColumn(5).setPreferredWidth(150);

        JScrollPane scrollPane = new JScrollPane(rankingTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 15));
        bottomPanel.setBackground(new Color(245, 247, 250));

        ModernButton refreshBtn = new ModernButton("刷新");
        refreshBtn.setFont(UiFont.font(Font.BOLD, 13));
        refreshBtn.addActionListener(e -> refreshRanking());

        ModernButton closeBtn = new ModernButton("关闭");
        closeBtn.setFont(UiFont.font(Font.BOLD, 13));
        closeBtn.addActionListener(e -> dispose());

        bottomPanel.add(refreshBtn);
        bottomPanel.add(closeBtn);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        // 加载数据
        refreshRanking();
        setVisible(true);
    }

    private void refreshRanking() {
        tableModel.setRowCount(0);
        List<GameRecordWithUser> records = UserManager.getInstance().getAllRecordsSorted();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        if (records.isEmpty()) {
            tableModel.addRow(new Object[]{"-", "暂无数据", "-", "-", "-", "-"});
            return;
        }

        int rank = 1;
        String lastDifficulty = null;
        int rankInDifficulty = 1;

        for (GameRecordWithUser entry : records) {
            String difficultyDisplay = getDifficultyDisplay(entry.record.difficulty);
            String timeFormatted = formatTime(entry.record.seconds);
            String dateStr = sdf.format(new Date(entry.record.timestamp));

            // 如果是新难度，重置该难度内的排名计数
            if (!entry.record.difficulty.equals(lastDifficulty)) {
                rankInDifficulty = 1;
                lastDifficulty = entry.record.difficulty;
            }

            // 排名显示：全局排名 + 该难度内排名
            String rankDisplay = rank + " (#" + rankInDifficulty + ")";

            Object[] row = {
                    rankDisplay,
                    entry.username,
                    difficultyDisplay,
                    timeFormatted,
                    entry.record.score,
                    dateStr
            };
            tableModel.addRow(row);

            rank++;
            rankInDifficulty++;
        }
    }

    private String getDifficultyDisplay(String difficultyName) {
        for (int i = 0; i < DIFFICULTY_NAMES.length; i++) {
            if (DIFFICULTY_NAMES[i].equals(difficultyName)) {
                return DISPLAY_NAMES[i];
            }
        }
        return difficultyName;
    }

    private String formatTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d时%02d分%02d秒", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d分%02d秒", minutes, secs);
        }
        return String.format("%d秒", secs);
    }

    /**
     * 静态方法：显示排行榜窗口
     */
    public static void showRanking() {
        SwingUtilities.invokeLater(RankingFrame::new);
    }
}