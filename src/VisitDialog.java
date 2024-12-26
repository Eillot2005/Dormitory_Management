import javax.swing.*;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class VisitDialog extends JDialog {
    private JTextField snoField;
    private JTextField reasonField;
    private JComboBox<String> visitDateComboBox;
    private JComboBox<String> visitTimeComboBox;
    private JComboBox<String> visitDurationComboBox;

    public VisitDialog(String id) {
        setTitle("来访登记");
        setSize(500, 350);
        setLayout(null);

        JLabel snoLabel = new JLabel("访问学生学号：");
        snoLabel.setBounds(20, 30, 120, 30);
        add(snoLabel);

        snoField = new JTextField();
        snoField.setBounds(150, 30, 300, 30);
        add(snoField);

        JLabel reasonLabel = new JLabel("来访原因：");
        reasonLabel.setBounds(20, 80, 120, 30);
        add(reasonLabel);

        reasonField = new JTextField();
        reasonField.setBounds(150, 80, 300, 30);
        add(reasonField);

        JLabel dateLabel = new JLabel("预计来访日期：");
        dateLabel.setBounds(20, 130, 120, 30);
        add(dateLabel);

        visitDateComboBox = new JComboBox<>();
        loadAvailableDates();
        visitDateComboBox.setBounds(150, 130, 300, 30);
        add(visitDateComboBox);

        JLabel timeLabel = new JLabel("预计来访时间：");
        timeLabel.setBounds(20, 180, 120, 30);
        add(timeLabel);

        visitTimeComboBox = new JComboBox<>();
        loadAvailableTimes();
        visitTimeComboBox.setBounds(150, 180, 300, 30);
        add(visitTimeComboBox);

        JLabel durationLabel = new JLabel("访问时长(小时)：");
        durationLabel.setBounds(20, 230, 120, 30);
        add(durationLabel);

        visitDurationComboBox = new JComboBox<>(new String[]{"0.5", "1", "1.5", "2", "2.5", "3", "3.5", "4"});
        visitDurationComboBox.setBounds(150, 230, 300, 30);
        add(visitDurationComboBox);

        JButton submitButton = new JButton("提交");
        submitButton.setBounds(200, 280, 100, 30);
        add(submitButton);

        submitButton.addActionListener(e -> handleSubmit(id));

        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * 加载可选日期：只能选择未来 7 天内的日期
     */
    private void loadAvailableDates() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date now = new Date();
        for (int i = 0; i <= 7; i++) {//未来7天
            Date futureDate = new Date(now.getTime() + i * 24 * 60 * 60 * 1000L);
            visitDateComboBox.addItem(sdf.format(futureDate));
        }
    }

    /**
     * 加载可选时间：以半小时为单位
     */
    private void loadAvailableTimes() {
        for (int hour = 8; hour <= 20; hour++) {//8:00-20:00
            visitTimeComboBox.addItem(String.format("%02d:00", hour));//%02d表示两位整数，不足两位前面补0
            visitTimeComboBox.addItem(String.format("%02d:30", hour));//%02d表示两位整数，不足两位前面补0
        }
    }

    /**
     * 提交登记信息
     */
    private void handleSubmit(String id) {
        String sno = snoField.getText().trim();
        String reason = reasonField.getText().trim();
        String date = (String) visitDateComboBox.getSelectedItem();
        String time = (String) visitTimeComboBox.getSelectedItem();
        String duration = (String) visitDurationComboBox.getSelectedItem();

        if (sno.isEmpty() || reason.isEmpty() || date == null || time == null || duration == null) {
            JOptionPane.showMessageDialog(this, "请填写所有信息！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            // 计算预计离开时间
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date visitStartTime = sdf.parse(date + " " + time);
            long durationInMillis = (long) (Double.parseDouble(duration) * 60 * 60 * 1000);
            Date visitEndTime = new Date(visitStartTime.getTime() + durationInMillis);

            // 将 java.util.Date 转换为 java.sql.Timestamp
            java.sql.Timestamp sqlVisitStartTime = new java.sql.Timestamp(visitStartTime.getTime());
            java.sql.Timestamp sqlVisitEndTime = new java.sql.Timestamp(visitEndTime.getTime());

            // 调用数据库方法
            if (DatabaseHelper.registerVisit(id, sno, reason, sqlVisitStartTime, sqlVisitEndTime)) {
                JOptionPane.showMessageDialog(this, "来访登记成功！请注意您的结束时间是"+sdf.format(visitEndTime), "成功", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 关闭窗口
                new VisitorFrame(id);
            } else {
                JOptionPane.showMessageDialog(this, "登记失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        } catch (ParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "日期或时间格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }
}

