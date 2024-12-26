import javax.swing.*;

public class RegisterFrame extends JFrame {
    public RegisterFrame() {
        setTitle("访客注册");
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

        JButton registerButton = new JButton("注册");
        registerButton.setBounds(150, 150, 100, 30);
        add(registerButton);

        registerButton.addActionListener(e -> {
            String username = userField.getText();
            String password = new String(passField.getPassword());

            if (DatabaseHelper.registerVisitor(username, password)) {
                JOptionPane.showMessageDialog(this, "注册成功，请登录！");
                dispose();
                new LoginFrame("访客", null);
            } else {
                JOptionPane.showMessageDialog(this, "注册失败，请重试！");
            }
        });

        setLocationRelativeTo(null);
        setVisible(true);
    }
}
