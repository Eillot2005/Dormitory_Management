import javax.swing.*;

public class MainLoginPanel {
    public MainLoginPanel() {
        JFrame frame = new JFrame("宿舍管理系统");
        frame.setSize(500, 400);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // 加载背景图片
        ImageIcon backgroundIcon = new ImageIcon(getClass().getResource("/1.png"));
        JLabel backgroundLabel = new JLabel(backgroundIcon);
        backgroundLabel.setBounds(0, 0, 500, 400); // 固定大小

        // 添加背景到最底层
        frame.getLayeredPane().add(backgroundLabel, JLayeredPane.DEFAULT_LAYER);
        // 设置内容面板透明
        JPanel contentPane = (JPanel) frame.getContentPane();
        contentPane.setOpaque(false);

        // 添加组件
        JLabel titleLabel = new JLabel("宿舍管理系统");
        titleLabel.setBounds(160, 30, 150, 30);
        titleLabel.setFont(titleLabel.getFont().deriveFont(24.0f));
        frame.add(titleLabel);

        JButton adminButton = new JButton("管理员登录");
        adminButton.setBounds(150, 100, 180, 50);
        frame.add(adminButton);

        JButton studentButton = new JButton("学生登录");
        studentButton.setBounds(150, 170, 180, 50);
        frame.add(studentButton);

        JButton visitorButton = new JButton("访客登录");
        visitorButton.setBounds(150, 240, 180, 50);
        frame.add(visitorButton);

        // 添加事件监听
        adminButton.addActionListener(e -> new LoginFrame("管理员", frame));
        studentButton.addActionListener(e -> new LoginFrame("学生", frame));
        visitorButton.addActionListener(e -> new VisitLoginFrame());

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}

