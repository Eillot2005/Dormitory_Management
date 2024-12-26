import javax.swing.*;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VisitLoginFrame extends JFrame {
    private JTextField idField;
    private JTextField phoneField;

    public VisitLoginFrame() {
        setTitle("访客登录");
        setSize(400, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 中心登录面板
        JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        loginPanel.add(new JLabel("身份证号:"));
        idField = new JTextField();
        loginPanel.add(idField);

        loginPanel.add(new JLabel("电话号码:"));
        phoneField = new JTextField();
        loginPanel.add(phoneField);

        JButton loginButton = new JButton("登录");
        JButton cancelButton = new JButton("取消");

        loginPanel.add(loginButton);
        loginPanel.add(cancelButton);

        add(loginPanel, BorderLayout.CENTER);

        // 按钮事件
        loginButton.addActionListener(e->{
            handleLogin();
            dispose();
        });
        cancelButton.addActionListener(e -> dispose());

        setVisible(true);
        setLocationRelativeTo(null);
    }

    /**
     * 处理访客登录逻辑
     */
    private void handleLogin() {
        String id = idField.getText().trim();//trim()方法用于去除字符串两端的空格
        String phone = phoneField.getText().trim();

        if (id.isEmpty() || phone.isEmpty()) {
            JOptionPane.showMessageDialog(this, "身份证号和电话号码不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if(!id.matches("\\d{17}[0-9X]") || !phone.matches("\\d{11}")) {//身份证号18位，电话号码11位
            JOptionPane.showMessageDialog(this, "身份证号或电话号码格式错误！", "错误", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (DatabaseHelper.isVisitorExists(id, phone)) {
            // 访客已存在，登录成功
            JOptionPane.showMessageDialog(this, "登录成功！欢迎回来！"+DatabaseHelper.getVisitorName(id));
            new VisitorFrame(id);
            dispose(); // 关闭登录窗口
        } else {
            // 首次登录，弹出姓名输入框
            String name = JOptionPane.showInputDialog(this, "首次登录，请输入您的姓名：", "首次登录", JOptionPane.PLAIN_MESSAGE);
            if (name == null || name.trim().isEmpty()) {
                JOptionPane.showMessageDialog(this, "姓名不能为空！", "错误", JOptionPane.ERROR_MESSAGE);
                return;
            }

            boolean success = DatabaseHelper.registerVisitor(id, phone, name.trim());
            if (success) {
                JOptionPane.showMessageDialog(this, "注册成功！欢迎访问！");
                new VisitorFrame(id);
                dispose(); // 关闭登录窗口
            } else {
                JOptionPane.showMessageDialog(this, "注册失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

