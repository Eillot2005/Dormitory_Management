import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class RepairManagement extends JFrame {
    private JTable repairTable;
    private DefaultTableModel repairTableModel;

    public RepairManagement() {
        setTitle("报修信息管理");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 中部表格
        repairTableModel = new DefaultTableModel(new String[]{"报修编号", "寝室号", "报修内容", "报修时间", "是否已解决", "备注"}, 0);
        repairTable = new JTable(repairTableModel);
        JScrollPane tableScrollPane = new JScrollPane(repairTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // 底部功能按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton markResolvedButton = new JButton("标记为已解决");
        JButton deleteRepairButton = new JButton("删除报修记录");
        bottomPanel.add(markResolvedButton);
        bottomPanel.add(deleteRepairButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // 加载报修信息
        loadRepairData();

        // 按钮事件绑定
        markResolvedButton.addActionListener(e -> markAsResolved());
        deleteRepairButton.addActionListener(e -> deleteRepair());

        setVisible(true);
        setLocationRelativeTo(null);
    }

    /**
     * 加载报修信息到表格
     */
    private void loadRepairData() {
        repairTableModel.setRowCount(0); // 清空表格
        try (ResultSet rs = DatabaseHelper.getAllRepairs()) {
            while (rs.next()) {
                repairTableModel.addRow(new Object[]{
                        rs.getString("FNO"),
                        rs.getString("RNO"),
                        rs.getString("FDetail"),
                        rs.getTimestamp("FTime"),
                        rs.getBoolean("FBool") ? "已解决" : "未解决",
                        rs.getString("FRemark")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载报修信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 标记为已解决
     */
    private void markAsResolved() {
        int selectedRow = repairTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要标记为已解决的报修记录！");
            return;
        }

        String repairId = (String) repairTableModel.getValueAt(selectedRow, 0); // 获取报修编号
        boolean success = DatabaseHelper.markRepairAsResolved(repairId);
        if (success) {
            JOptionPane.showMessageDialog(this, "报修记录已标记为已解决！");
            loadRepairData(); // 重新加载报修信息
        } else {
            JOptionPane.showMessageDialog(this, "标记失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 删除报修记录
     */
    private void deleteRepair() {
        int selectedRow = repairTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的报修记录！");
            return;
        }

        String repairId = (String) repairTableModel.getValueAt(selectedRow, 0); // 获取报修编号
        int option = JOptionPane.showConfirmDialog(this, "确认删除报修编号为 " + repairId + " 的记录？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteRepair(repairId);
            if (success) {
                JOptionPane.showMessageDialog(this, "报修记录删除成功！");
                loadRepairData(); // 重新加载报修信息
            } else {
                JOptionPane.showMessageDialog(this, "删除失败，请重试！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
