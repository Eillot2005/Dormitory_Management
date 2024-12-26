import javax.swing.*;

public class LoginFrame extends JFrame {
    public LoginFrame(String role, JFrame parent) {
        setTitle(role + "登录");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel userLabel = new JLabel("用户名：");
        userLabel.setBounds(50, 50, 100, 30);
        add(userLabel);

        JTextField userField = new JTextField();
        userField.setBounds(150, 50, 200, 30);
        add(userField);

        JLabel passLabel = new JLabel("密码：");
        passLabel.setBounds(50, 100, 100, 30);
        add(passLabel);

        JPasswordField passField = new JPasswordField();
        passField.setBounds(150, 100, 200, 30);
        add(passField);

        JButton loginButton = new JButton("登录");
        loginButton.setBounds(150, 150, 100, 30);
        add(loginButton);

        JButton registerButton = new JButton("注册");
        registerButton.setBounds(260, 150, 100, 30);
        if (role.equals("访客")) add(registerButton);

        loginButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (DatabaseHelper.login(role, username, password)) {
                JOptionPane.showMessageDialog(this, role + "登录成功！");
                // 关闭parent窗口
                parent.setVisible(false);
                dispose();
                switch (role) {
                    case "管理员" -> new AdminFrame(username);
                    case "学生" -> new StudentFrame(username);
                    case "访客" -> new VisitorFrame(username);
                }
            } else {
                JOptionPane.showMessageDialog(this, "用户名或密码错误！");
            }
        });

        registerButton.addActionListener(e -> {
            dispose();
            new RegisterFrame();
        });
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
