import javax.swing.*;

public class AdminFrame extends JFrame {
    public AdminFrame(String username) {
        setTitle("管理员功能界面");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(null);

        JLabel welcomeLabel = new JLabel("欢迎，管理员：" + username);
        welcomeLabel.setBounds(20, 20, 300, 30);
        add(welcomeLabel);

        JButton viewStudentsButton = new JButton("查看管理学生信息");
        viewStudentsButton.setBounds(50, 100, 150, 30);
        add(viewStudentsButton);

        JButton manageRepairsButton = new JButton("查看管理报修信息");
        manageRepairsButton.setBounds(50, 150, 150, 30);
        add(manageRepairsButton);

        JButton manageVisitsButton = new JButton("查看管理来访记录");
        manageVisitsButton.setBounds(50, 200, 150, 30);
        add(manageVisitsButton);

        viewStudentsButton.addActionListener(e -> new StudentManagement());
        manageRepairsButton.addActionListener(e -> new RepairManagement());
        manageVisitsButton.addActionListener(e -> new VisitManagement());
        setLocationRelativeTo(null);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
