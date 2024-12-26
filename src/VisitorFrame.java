import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class VisitorFrame extends JFrame {
    private JLabel countdownLabel; // 倒计时标签
    private Timer timer; // 定时器

    public VisitorFrame(String id) {
        setTitle("访客功能界面");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        // 欢迎标签
        JLabel welcomeLabel = new JLabel("欢迎，访客：" + DatabaseHelper.getVisitorName(id));
        welcomeLabel.setBounds(20, 20, 400, 30);
        welcomeLabel.setFont(new Font("微软雅黑", Font.BOLD, 16));
        add(welcomeLabel);

        // 按钮 - 登记来访
        JButton visitRegisterButton = new JButton("登记来访");
        visitRegisterButton.setBounds(50, 100, 150, 30);
        add(visitRegisterButton);

        // 按钮 - 查看历史记录
        JButton viewHistoryButton = new JButton("查看历史记录");
        viewHistoryButton.setBounds(50, 150, 150, 30);
        add(viewHistoryButton);

        // 按钮 - 提前结束
        JButton endVisitButton = new JButton("提前结束");
        endVisitButton.setBounds(50, 250, 150, 30);
        add(endVisitButton);

        // 倒计时模块 - 放在右边
        countdownLabel = new JLabel("距离离校时间剩余：--:--:--", SwingConstants.CENTER);
        countdownLabel.setBounds(300, 100, 350, 200);
        countdownLabel.setFont(new Font("微软雅黑", Font.BOLD, 24)); // 设置大字体
        countdownLabel.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 2)); // 设置边框
        add(countdownLabel);

        // 按钮 - 延长时间
        JButton extendTimeButton = new JButton("延长时间");
        extendTimeButton.setBounds(50, 200, 150, 30);
        add(extendTimeButton);

        // 事件绑定
        //注册按钮事件，有多句代码，用{}括起来
        visitRegisterButton.addActionListener(e->{
            new VisitDialog(id); // 打开登记对话框
            dispose(); // 关闭当前窗口
        });
        viewHistoryButton.addActionListener(e -> showVisitHistory(id)); // 显示历史记录
        extendTimeButton.addActionListener(e -> handleExtendTime(id)); // 延长访问时间
        endVisitButton.addActionListener(e -> {
            DatabaseHelper.endVisitorVisit(id);
            JOptionPane.showMessageDialog(this, "访问已结束！");
            dispose(); // 关闭当前窗口
        });

        // 初始化倒计时
        startCountdown(id);

        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * 开始倒计时
     */
    private void startCountdown(String id) {
        if (timer != null) {
            timer.cancel(); // 停止之前的倒计时
        }

        Date leaveTime = DatabaseHelper.getVisitorLeaveTime(id); // 获取访客的离校时间
        if (leaveTime == null) {
            countdownLabel.setText("当前没有正在进行的访问记录");
            return;
        }

        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                long timeLeft = leaveTime.getTime() - System.currentTimeMillis();
                if (timeLeft <= 0) {
                    countdownLabel.setText("距离离校时间剩余：已过期");
                    timer.cancel();
                } else {
                    long hours = timeLeft / (1000 * 60 * 60);
                    long minutes = (timeLeft / (1000 * 60)) % 60;
                    long seconds = (timeLeft / 1000) % 60;
                    countdownLabel.setText(String.format("距离离校时间剩余：%02d:%02d:%02d", hours, minutes, seconds));
                }
            }
        }, 0, 1000); // 每秒更新一次
    }

    /**
     * 处理延长时间逻辑
     */
    private void handleExtendTime(String id) {
        String extraTime = (String) JOptionPane.showInputDialog(this, "选择增加的时长(小时)：", "延长时间",
                JOptionPane.PLAIN_MESSAGE, null, new String[]{"0.5", "1", "1.5", "2"}, "0.5");

        if (extraTime != null) {
            double additionalHours = Double.parseDouble(extraTime);
            boolean success = DatabaseHelper.extendVisitorLeaveTime(id, additionalHours); // 更新数据库中的离校时间
            if (success) {
                JOptionPane.showMessageDialog(this, "访问时间已延长！");
                startCountdown(id); // 重新启动倒计时
                //重新打开窗口
                new VisitorFrame(id);
                dispose(); // 关闭当前窗口
            } else {
                JOptionPane.showMessageDialog(this, "延长时间失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 显示访客历史记录
     */
    private void showVisitHistory(String visitorId) {
        // 创建对话框
        JDialog historyDialog = new JDialog(this, "历史记录", true);
        historyDialog.setSize(600, 400);
        historyDialog.setLayout(new BorderLayout());

        // 表头
        String[] columnNames = {"访问编号", "访问学生学号", "访问原因", "来访时间", "离校时间"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        JTable historyTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(historyTable);

        // 从数据库加载数据
        List<Object[]> historyData = DatabaseHelper.getVisitorHistory(visitorId);
        for (Object[] row : historyData) {
            tableModel.addRow(row);
        }

        // 设置表格样式
        historyTable.setFillsViewportHeight(true);
        historyTable.setRowHeight(25);

        // 添加到对话框
        historyDialog.add(scrollPane, BorderLayout.CENTER);

        // 关闭按钮
        JButton closeButton = new JButton("关闭");
        closeButton.addActionListener(e -> historyDialog.dispose());
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(closeButton);
        historyDialog.add(buttonPanel, BorderLayout.SOUTH);

        historyDialog.setLocationRelativeTo(this);
        historyDialog.setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
