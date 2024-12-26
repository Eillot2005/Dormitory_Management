import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class VisitManagement extends JFrame {
    private JTable visitTable, visitorTable; // 表格：来访登记和访客信息
    private DefaultTableModel visitTableModel, visitorTableModel;
    private JTextField searchVisitorField, searchVisitField; // 搜索框

    public VisitManagement() {
        setTitle("访客信息管理");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 上部分：访客信息表格
        JPanel visitorPanel = new JPanel(new BorderLayout());
        visitorPanel.setBorder(BorderFactory.createTitledBorder("访客信息"));
        visitorTableModel = new DefaultTableModel(new String[]{"访客身份证号", "姓名", "电话"}, 0);
        visitorTable = new JTable(visitorTableModel);
        visitorPanel.add(new JScrollPane(visitorTable), BorderLayout.CENTER);

        // 搜索框和按钮（访客信息）
        JPanel visitorSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        visitorSearchPanel.add(new JLabel("查找访客："));
        searchVisitorField = new JTextField(20);
        JButton searchVisitorButton = new JButton("搜索");
        visitorSearchPanel.add(searchVisitorField);
        visitorSearchPanel.add(searchVisitorButton);
        visitorPanel.add(visitorSearchPanel, BorderLayout.NORTH);

        // 访客管理按钮
        JPanel visitorButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton deleteVisitorButton = new JButton("删除访客信息");
        visitorButtonPanel.add(deleteVisitorButton);
        visitorPanel.add(visitorButtonPanel, BorderLayout.SOUTH);

        // 下部分：来访登记表格
        JPanel visitPanel = new JPanel(new BorderLayout());
        visitPanel.setBorder(BorderFactory.createTitledBorder("来访登记"));
        visitTableModel = new DefaultTableModel(new String[]{"登记编号", "访客名", "访客身份证号", "被访学号", "被访学生", "来访时间", "离开时间", "原因"}, 0);
        visitTable = new JTable(visitTableModel);
        visitPanel.add(new JScrollPane(visitTable), BorderLayout.CENTER);

        // 搜索框和按钮（来访登记）
        JPanel visitSearchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        visitSearchPanel.add(new JLabel("查找来访记录："));
        searchVisitField = new JTextField(20);
        JButton searchVisitButton = new JButton("搜索");
        visitSearchPanel.add(searchVisitField);
        visitSearchPanel.add(searchVisitButton);
        visitPanel.add(visitSearchPanel, BorderLayout.NORTH);

        // 来访登记管理按钮
        JPanel visitButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton deleteVisitButton = new JButton("删除来访记录");
        visitButtonPanel.add(deleteVisitButton);
        visitPanel.add(visitButtonPanel, BorderLayout.SOUTH);

        // 主界面布局
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, visitorPanel, visitPanel);
        splitPane.setDividerLocation(300);
        add(splitPane, BorderLayout.CENTER);

        // 加载数据
        loadVisitorData(null); // 初始加载所有访客信息
        loadVisitData(null); // 初始加载所有来访记录

        // 按钮事件绑定
        deleteVisitorButton.addActionListener(e -> deleteVisitor());
        deleteVisitButton.addActionListener(e -> deleteVisit());
        searchVisitorButton.addActionListener(e -> searchVisitor());
        searchVisitButton.addActionListener(e -> searchVisit());

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * 加载访客信息数据到表格
     * @param searchCondition 搜索条件，可以为 null 表示加载所有数据
     */
    private void loadVisitorData(String searchCondition) {
        visitorTableModel.setRowCount(0); // 清空表格
        java.util.List<String[]> visitorList = DatabaseHelper.searchVisitors(searchCondition);
        for (String[] visitor : visitorList) {
            visitorTableModel.addRow(visitor);
        }
    }


    /**
     * 加载来访登记数据到表格
     * @param searchCondition 搜索条件，可以为 null 表示加载所有数据
     */
    private void loadVisitData(String searchCondition) {
        visitTableModel.setRowCount(0); // 清空表格
        java.util.List<String[]> visitList = DatabaseHelper.searchVisits(searchCondition);
        // 调试日志
        System.out.println("加载到的来访记录数：" + visitList.size());
        // 将记录加载到表格
        for (String[] visit : visitList) {
            visitTableModel.addRow(visit);
        }
        if (visitList.isEmpty()) {
            JOptionPane.showMessageDialog(this, "没有找到符合条件的来访记录！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }



    /**
     * 搜索访客信息
     */
    private void searchVisitor() {
        String condition = searchVisitorField.getText().trim();
        if (condition.isEmpty()) {
            loadVisitorData(null); // 加载所有访客
        } else {
            loadVisitorData(condition); // 根据条件搜索
        }
    }

    /**
     * 搜索来访记录
     */
    private void searchVisit() {
        String condition = searchVisitField.getText().trim();
        if (condition.isEmpty()) {
            loadVisitData(null); // 加载所有来访记录
        } else {
            loadVisitData(condition); // 根据条件搜索
        }
    }

    /**
     * 删除访客信息
     */
    private void deleteVisitor() {
        int selectedRow = visitorTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的访客信息！");
            return;
        }

        String visitorId = (String) visitorTableModel.getValueAt(selectedRow, 0); // 获取访客身份证号
        int option = JOptionPane.showConfirmDialog(this, "确认删除身份证号为 " + visitorId + " 的访客信息？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteVisitor(visitorId);
            if (success) {
                JOptionPane.showMessageDialog(this, "访客信息删除成功！");
                loadVisitorData(null); // 重新加载访客信息
            } else {
                JOptionPane.showMessageDialog(this, "删除访客信息失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 删除来访记录
     */
    private void deleteVisit() {
        int selectedRow = visitTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的来访记录！");
            return;
        }

        String visitId = (String) visitTableModel.getValueAt(selectedRow, 0); // 获取登记编号
        int option = JOptionPane.showConfirmDialog(this, "确认删除登记编号为 " + visitId + " 的来访记录？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteVisitRecord(visitId);
            if (success) {
                JOptionPane.showMessageDialog(this, "来访记录删除成功！");
                loadVisitData(null); // 重新加载来访记录
            } else {
                JOptionPane.showMessageDialog(this, "删除来访记录失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

