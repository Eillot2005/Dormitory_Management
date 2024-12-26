import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;

public class RepairDialog extends JDialog {
    public RepairDialog(String studentSNO) {
        setTitle("报修登记");
        setSize(400, 300);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

// 创建提示文字栏目
        String roomNumber = DatabaseHelper.getStudentRoomNumber(studentSNO);
        JLabel roomLabel = new JLabel("当前报修寝室号：" + roomNumber);
        JLabel detailLabel = new JLabel("报修内容：");
        JTextField detailField = new JTextField(30);
        JButton submitButton = new JButton("提交");
        JButton repairHistoryButton = new JButton("查看报修历史");

// 使用 GridBagLayout 布局
        // 使用 GridBagLayout 布局
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);//设置组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.CENTER; // 将内容居中

        // 第一行：寝室号标签
        gbc.gridx = 0;//设置组件所在的行
        gbc.gridy = 0;//设置组件所在的列
        gbc.gridwidth = 3;//设置组件所占的行数
        gbc.weightx = 0;//设置组件的宽度
        add(roomLabel, gbc);

        // 第二行：报修内容标签
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        add(detailLabel, gbc);

        // 第二行：报修内容输入框
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        gbc.weightx = 1;
        add(detailField, gbc);

        // 第三行：提交按钮
        gbc.gridx = 1;
        gbc.gridy = 2;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        add(submitButton, gbc);

        // 第三行：查看报修历史按钮
        gbc.gridx = 1;
        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.weightx = 0;
        add(repairHistoryButton, gbc);

        setLocationRelativeTo(null);
        setVisible(true);

        // 提交报修信息
        submitButton.addActionListener(e -> {
            String detail = detailField.getText();
            if (detail.isEmpty()) {
                JOptionPane.showMessageDialog(this, "报修内容不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }
            // 调用数据库操作，插入报修信息
            if (DatabaseHelper.registerRepair(roomNumber, detail)) {
                JOptionPane.showMessageDialog(this, "报修提交成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                dispose(); // 关闭对话框
            } else {
                JOptionPane.showMessageDialog(this, "报修提交失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 查看报修历史
        repairHistoryButton.addActionListener(e -> {
            // 从数据库中获取报修记录
            String sql = "SELECT FDetail, FTime, FBool, FRemark, RNO FROM 报修信息 WHERE RNO = ?";
            try (Connection conn = DatabaseHelper.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, roomNumber);
                ResultSet rs = stmt.executeQuery();

                // 创建一个表格来显示报修记录
                String[] columnNames = {"报修内容", "报修时间", "是否已解决", "备注", "寝室号"};
                Object[][] data = extractData(rs, columnNames.length);

                // 创建一个新的 JFrame 来显示报修历史
                JFrame historyFrame = new JFrame("报修历史");
                historyFrame.setSize(800, 400);

                JTable table = new JTable(data, columnNames);
                JScrollPane scrollPane = new JScrollPane(table);
                historyFrame.add(scrollPane);

                historyFrame.setVisible(true);

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "查询报修记录失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private static Object[][] extractData(ResultSet rs, int columnCount) throws SQLException {
        // 使用动态数组存储数据
        java.util.List<Object[]> dataList = new java.util.ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
        while (rs.next()) {
            Object[] row = new Object[columnCount];
            for (int i = 0; i < columnCount; i++) {
                if (i == 1) { // 第二列是时间，格式化为字符串
                    row[i] = dateFormat.format(rs.getTimestamp(i + 1));
                } else {// 其他列直接获取
                    row[i] = rs.getObject(i + 1);
                }
            }
            dataList.add(row);
        }
        // 将 List 转为二维数组
        return dataList.toArray(new Object[0][0]);
    }
}
