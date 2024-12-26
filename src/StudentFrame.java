import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentFrame extends JFrame {
    public StudentFrame(String studentSNO) {
        setTitle("学生功能界面");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("欢迎，学生：" + DatabaseHelper.getStudentName(studentSNO));
        welcomeLabel.setBounds(20, 20, 300, 30);
        add(welcomeLabel);

        JButton personalInfoButton = new JButton("查看个人信息");
        personalInfoButton.setBounds(50, 100, 150, 30);
        add(personalInfoButton);

        JButton repairButton = new JButton("报修登记");
        repairButton.setBounds(50, 150, 150, 30);
        add(repairButton);

        JButton roomInfoButton = new JButton("查看寝室入住情况");
        roomInfoButton.setBounds(50, 200, 150, 30);
        add(roomInfoButton);

        JButton updatePasswordButton = new JButton("修改密码");
        updatePasswordButton.setBounds(50, 250, 150, 30);
        add(updatePasswordButton);

        // 查看个人信息
        personalInfoButton.addActionListener(e -> {
            ResultSet rs = DatabaseHelper.getStudentInfo(studentSNO);
            try {
                if (rs != null && rs.next()) {
                    String info = "学号: " + rs.getString("SNO") + "\n" +
                            "姓名: " + rs.getString("SName") + "\n" +
                            "性别: " + rs.getString("SSex") + "\n" +
                            "学院: " + rs.getString("SCollege") + "\n" +
                            "班级: " + rs.getString("SClass") + "\n" +
                            "宿舍号: " + DatabaseHelper.getStudentRoomNumber(studentSNO);
                    JOptionPane.showMessageDialog(this, info, "个人信息", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "无法获取个人信息！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } catch (SQLException ex) {
                ex.printStackTrace();//打印异常信息
                JOptionPane.showMessageDialog(this, "数据库查询出错！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        });

        // 报修登记
        repairButton.addActionListener(e -> {
            if(DatabaseHelper.getStudentRoomNumber(studentSNO) == null || DatabaseHelper.getStudentRoomNumber(studentSNO).isEmpty()) {
                JOptionPane.showMessageDialog(this, "你还没有被分配寝室！", "提示", JOptionPane.WARNING_MESSAGE);
                return;
            }else {
                new RepairDialog(studentSNO);
            }
        });

        // 查看寝室入住情况
        roomInfoButton.addActionListener(e -> {
            String roomNumber = DatabaseHelper.getStudentRoomNumber(studentSNO);
            if (roomNumber != null && !roomNumber.isEmpty()) {
                ResultSet rs = DatabaseHelper.getRoomStatus(roomNumber);
                StringBuilder sb = new StringBuilder("寝室号: " + roomNumber + "\n入住学生信息:\n");
                sb.append("本寝室应住人数: ").append(DatabaseHelper.getRoomCapacity(roomNumber)).append("\n");
                sb.append(String.format("%-10s %-10s %-6s %-10s %-8s %-15s\n",
                        "学号", "姓名", "性别", "学院", "班级", "电话"));//设置表头
                try {
                    boolean hasData = false;
                    while (rs != null && rs.next()) {
                        hasData = true;
                        sb.append(String.format("%-10s %-10s %-6s %-10s %-8s %-15s\n",//设置表格格式,每个字段占位符的宽度,左对齐
                                rs.getString("SNO"),
                                rs.getString("SName"),
                                rs.getString("SSex"),
                                rs.getString("SCollege"),
                                rs.getString("SClass"),
                                rs.getString("SPhone")));
                    }//设置表格内容
                    if (!hasData) {
                        sb.append("暂无入住学生信息！");
                    }
                    //创建一个新的窗口显示寝室信息
                    JFrame roomInfoFrame = new JFrame("寝室入住情况");
                    roomInfoFrame.setSize(500, 300);
                    roomInfoFrame.setLayout(new BorderLayout());
                    JTextArea textArea = new JTextArea();
                    textArea.setText(sb.toString());
                    textArea.setEditable(false);
                    roomInfoFrame.add(new JScrollPane(textArea), BorderLayout.CENTER);
                    JButton closeButton = new JButton("关闭");
                    closeButton.addActionListener(event -> roomInfoFrame.dispose());
                    roomInfoFrame.add(closeButton, BorderLayout.SOUTH);
                    roomInfoFrame.setLocationRelativeTo(this);
                    roomInfoFrame.setVisible(true);
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "数据库查询出错！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "你还没有被分配寝室！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        // 修改密码
        updatePasswordButton.addActionListener(e -> {
            String newPassword = JOptionPane.showInputDialog(this, "请输入新密码：", "修改密码", JOptionPane.PLAIN_MESSAGE);
            if (newPassword != null && !newPassword.isEmpty()) {
                String password=JOptionPane.showInputDialog(this, "请再次输入新密码：", "修改密码", JOptionPane.PLAIN_MESSAGE);
                if(password.equals(newPassword)){
                    if(DatabaseHelper.updateStudentPassword(studentSNO, newPassword)){
                        JOptionPane.showMessageDialog(this, "密码修改成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
                    }
                    else{
                        JOptionPane.showMessageDialog(this, "密码修改失败！", "提示", JOptionPane.ERROR_MESSAGE);
                    }
                }
                else{
                    JOptionPane.showMessageDialog(this, "两次输入的密码不一致！", "提示", JOptionPane.WARNING_MESSAGE);
                }
            }
            else{
                JOptionPane.showMessageDialog(this, "新密码不能为空！", "提示", JOptionPane.WARNING_MESSAGE);
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}

