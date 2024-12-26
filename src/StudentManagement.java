import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StudentManagement extends JFrame {
    private JTable studentTable; // 学生信息表格
    private DefaultTableModel studentTableModel;
    private JComboBox<String> dormitoryComboBox;
    private JTextField snoField, nameField, collegeField, classField;

    public StudentManagement() {
        setTitle("学生信息管理");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // 顶部查询面板
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("学号:"));
        snoField = new JTextField(10);
        topPanel.add(snoField);

        topPanel.add(new JLabel("姓名:"));
        nameField = new JTextField(10);
        topPanel.add(nameField);

        topPanel.add(new JLabel("学院:"));
        collegeField = new JTextField(10);
        topPanel.add(collegeField);

        topPanel.add(new JLabel("班级:"));
        classField = new JTextField(10);
        topPanel.add(classField);

        topPanel.add(new JLabel("寝室:"));
        dormitoryComboBox = new JComboBox<>();
        loadDormitoryData(); // 加载寝室信息到下拉框
        topPanel.add(dormitoryComboBox);

        JButton searchButton = new JButton("查询");
        topPanel.add(searchButton);

        add(topPanel, BorderLayout.NORTH);

        // 中部表格
        studentTableModel = new DefaultTableModel(new String[]{"学号", "姓名", "性别", "电话", "学院", "班级", "寝室"}, 0);
        studentTable = new JTable(studentTableModel);
        JScrollPane tableScrollPane = new JScrollPane(studentTable);
        add(tableScrollPane, BorderLayout.CENTER);

        // 底部功能按钮面板
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addStudentButton = new JButton("添加学生");
        JButton updateStudentButton = new JButton("修改学生");
        JButton deleteStudentButton = new JButton("删除学生");
        JButton manageDormitoryButton = new JButton("管理寝室信息");
        JButton manageStudentDormitoryButton = new JButton("管理学生寝室");

        bottomPanel.add(addStudentButton);
        bottomPanel.add(updateStudentButton);
        bottomPanel.add(deleteStudentButton);
        bottomPanel.add(manageDormitoryButton);
        bottomPanel.add(manageStudentDormitoryButton);

        add(bottomPanel, BorderLayout.SOUTH);

        // 加载所有学生信息
        loadStudentData(null, null, null, null, null);

        // 按钮事件绑定
        searchButton.addActionListener(e -> searchStudents());
        addStudentButton.addActionListener(e -> addStudent());
        updateStudentButton.addActionListener(e -> updateStudent());
        deleteStudentButton.addActionListener(e -> deleteStudent());
        manageDormitoryButton.addActionListener(e -> manageDormitory());
        manageStudentDormitoryButton.addActionListener(e -> manageStudentDormitory());

        setVisible(true);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    /**
     * 加载寝室数据到下拉框
     */
    private void loadDormitoryData() {
        dormitoryComboBox.addItem(""); // 添加空选项
        try (ResultSet rs = DatabaseHelper.getAllDormitories()) {
            while (rs.next()) {
                dormitoryComboBox.addItem(rs.getString("RNO"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载寝室信息失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载学生数据到表格
     */
    private void loadStudentData(String sno, String name, String college, String sClass, String dormitory) {
        studentTableModel.setRowCount(0); // 清空表格
        try (ResultSet rs = DatabaseHelper.getStudents(sno, name, college, sClass, dormitory)) {
            while (rs.next()) {
                studentTableModel.addRow(new Object[]{
                        rs.getString("SNO"),
                        rs.getString("SName"),
                        rs.getString("SSex"),
                        rs.getString("SPhone"),
                        rs.getString("SCollege"),
                        rs.getString("SClass"),
                        rs.getString("RNO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载学生数据失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 查询学生信息
     */
    private void searchStudents() {
        String sno = snoField.getText();
        String name = nameField.getText();
        String college = collegeField.getText();
        String sClass = classField.getText();
        String dormitory = (String) dormitoryComboBox.getSelectedItem();
        loadStudentData(sno, name, college, sClass, dormitory);
    }

    /**
     * 添加学生
     */
    private void addStudent() {
        JTextField snoField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField sexField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField collegeField = new JTextField();
        JTextField classField = new JTextField();
        // 输入框
        Object[] inputFields = {
                "学号:", snoField,
                "姓名:", nameField,
                "性别:", sexField,
                "电话:", phoneField,
                "学院:", collegeField,
                "班级:", classField
        };
        // 显示输入框
        int option = JOptionPane.showConfirmDialog(this, inputFields, "添加学生", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {// 确认添加
            //如果学号不为8位
            if (snoField.getText().length() != 8) {
                JOptionPane.showMessageDialog(this, "学号必须为8位！", "错误", JOptionPane.ERROR_MESSAGE);
                snoField.setText("");
            }
            //如果学号已存在
            if (DatabaseHelper.isStudentExist(snoField.getText())) {
                JOptionPane.showMessageDialog(this, "学号已存在！", "错误", JOptionPane.ERROR_MESSAGE);
                snoField.setText("");
            }
            //如果电话号码不为11位
            if (phoneField.getText().length() != 11) {
                JOptionPane.showMessageDialog(this, "电话号码必须为11位！", "错误", JOptionPane.ERROR_MESSAGE);
                phoneField.setText("");
            }
            //如果性别不为男或女
            if (!sexField.getText().equals("男") && !sexField.getText().equals("女")) {
                JOptionPane.showMessageDialog(this, "性别必须为男或女！", "错误", JOptionPane.ERROR_MESSAGE);
                //清空输入框
                sexField.setText("");
            }
            boolean success = DatabaseHelper.addStudent(
                    snoField.getText(),
                    nameField.getText(),
                    sexField.getText(),
                    phoneField.getText(),
                    collegeField.getText(),
                    classField.getText()
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "学生添加成功！");
                searchStudents(); // 重新加载数据
            } else {
                JOptionPane.showMessageDialog(this, "学生添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 修改学生信息
     */
    private void updateStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要修改的学生！");
            return;
        }

        String sno = (String) studentTableModel.getValueAt(selectedRow, 0);
        JTextField nameField = new JTextField((String) studentTableModel.getValueAt(selectedRow, 1));
        JTextField sexField = new JTextField((String) studentTableModel.getValueAt(selectedRow, 2));
        JTextField phoneField = new JTextField((String) studentTableModel.getValueAt(selectedRow, 3));
        JTextField collegeField = new JTextField((String) studentTableModel.getValueAt(selectedRow, 4));
        JTextField classField = new JTextField((String) studentTableModel.getValueAt(selectedRow, 5));
        JTextField passwordField = new JTextField(DatabaseHelper.getStudentPassword(sno));

        Object[] inputFields = {
                "姓名:", nameField,
                "性别:", sexField,
                "电话:", phoneField,
                "学院:", collegeField,
                "班级:", classField,
                "密码:", passwordField,
        };
        int option = JOptionPane.showConfirmDialog(this, inputFields, "修改学生信息", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            boolean success = DatabaseHelper.updateStudent(
                    sno,
                    nameField.getText(),
                    sexField.getText(),
                    phoneField.getText(),
                    collegeField.getText(),
                    classField.getText(),
                    passwordField.getText()
            );
            if (success) {
                JOptionPane.showMessageDialog(this, "学生信息修改成功！");
                searchStudents(); // 重新加载数据
            } else {
                JOptionPane.showMessageDialog(this, "学生信息修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 删除学生
     */
    private void deleteStudent() {
        int selectedRow = studentTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的学生！");
            return;
        }

        String sno = (String) studentTableModel.getValueAt(selectedRow, 0);
        int option = JOptionPane.showConfirmDialog(this, "确认删除学号为 " + sno + " 的学生？", "确认删除", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            boolean success = DatabaseHelper.deleteStudent(sno);
            if (success) {
                JOptionPane.showMessageDialog(this, "学生删除成功！");
                searchStudents(); // 重新加载数据
            } else {
                JOptionPane.showMessageDialog(this, "学生删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 管理寝室信息
     */
    private void manageDormitory() {
        JFrame dormitoryFrame = new JFrame("寝室信息管理");
        dormitoryFrame.setSize(600, 400);
        dormitoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        dormitoryFrame.setLayout(new BorderLayout());

        // 寝室信息表格
        DefaultTableModel dormitoryTableModel = new DefaultTableModel(new String[]{"寝室号", "当前人数", "最大人数"}, 0);
        JTable dormitoryTable = new JTable(dormitoryTableModel);
        JScrollPane scrollPane = new JScrollPane(dormitoryTable);
        dormitoryFrame.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addDormitoryButton = new JButton("添加寝室");
        JButton updateDormitoryButton = new JButton("修改寝室");
        JButton deleteDormitoryButton = new JButton("删除寝室");

        bottomPanel.add(addDormitoryButton);
        bottomPanel.add(updateDormitoryButton);
        bottomPanel.add(deleteDormitoryButton);
        dormitoryFrame.add(bottomPanel, BorderLayout.SOUTH);

        // 加载寝室信息
        loadDormitoryTableData(dormitoryTableModel);

        // 按钮事件
        addDormitoryButton.addActionListener(e -> {
            JTextField dormitoryNumberField = new JTextField();
            JTextField maxCapacityField = new JTextField();

            Object[] inputFields = {
                    "寝室号:", dormitoryNumberField,
                    "最大人数:", maxCapacityField
            };

            int option = JOptionPane.showConfirmDialog(dormitoryFrame, inputFields, "添加寝室", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                boolean success = DatabaseHelper.addDormitory(
                        dormitoryNumberField.getText(),
                        Integer.parseInt(maxCapacityField.getText())
                );
                if (success) {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室添加成功！");
                    loadDormitoryTableData(dormitoryTableModel);
                } else {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室添加失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        updateDormitoryButton.addActionListener(e -> {
            int selectedRow = dormitoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dormitoryFrame, "请选择要修改的寝室！");
                return;
            }

            String dormitoryNumber = (String) dormitoryTableModel.getValueAt(selectedRow, 0);
            JTextField maxCapacityField = new JTextField(dormitoryTableModel.getValueAt(selectedRow, 2).toString());

            Object[] inputFields = {
                    "最大人数:", maxCapacityField
            };

            int option = JOptionPane.showConfirmDialog(dormitoryFrame, inputFields, "修改寝室信息", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                boolean success = DatabaseHelper.updateDormitory(
                        dormitoryNumber,
                        Integer.parseInt(maxCapacityField.getText())
                );
                if (success) {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室信息修改成功！");
                    loadDormitoryTableData(dormitoryTableModel);
                } else {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室信息修改失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        deleteDormitoryButton.addActionListener(e -> {
            int selectedRow = dormitoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(dormitoryFrame, "请选择要删除的寝室！");
                return;
            }

            String dormitoryNumber = (String) dormitoryTableModel.getValueAt(selectedRow, 0);
            int option = JOptionPane.showConfirmDialog(dormitoryFrame, "确认删除寝室号为 " + dormitoryNumber + " 的寝室？", "确认删除", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                boolean success = DatabaseHelper.deleteDormitory(dormitoryNumber);
                if (success) {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室删除成功！");
                    loadDormitoryTableData(dormitoryTableModel);
                } else {
                    JOptionPane.showMessageDialog(dormitoryFrame, "寝室删除失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        dormitoryFrame.setVisible(true);
        dormitoryFrame.setLocationRelativeTo(null);
    }

    /**
     * 加载寝室信息到表格
     */
    private void loadDormitoryTableData(DefaultTableModel dormitoryTableModel) {
        dormitoryTableModel.setRowCount(0); // 清空表格
        try (ResultSet rs = DatabaseHelper.getAllDormitories()) {
            while (rs.next()) {
                dormitoryTableModel.addRow(new Object[]{
                        rs.getString("RNO"),
                        rs.getInt("RNumber"),
                        rs.getInt("RMax")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载寝室数据失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }


    /**
     * 管理学生寝室信息
     */
    private void manageStudentDormitory() {
        JFrame studentDormitoryFrame = new JFrame("学生寝室管理");
        studentDormitoryFrame.setSize(600, 400);
        studentDormitoryFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        studentDormitoryFrame.setLayout(new BorderLayout());

        // 学生寝室表格
        DefaultTableModel studentDormitoryTableModel = new DefaultTableModel(new String[]{"学号", "姓名", "寝室号"}, 0);
        JTable studentDormitoryTable = new JTable(studentDormitoryTableModel);
        JScrollPane scrollPane = new JScrollPane(studentDormitoryTable);
        studentDormitoryFrame.add(scrollPane, BorderLayout.CENTER);

        // 底部按钮
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton assignDormitoryButton = new JButton("分配寝室");
        JButton removeStudentDormitoryButton = new JButton("移除学生寝室");
        JButton changeStudentDormitoryButton = new JButton("更换学生寝室");

        bottomPanel.add(assignDormitoryButton);
        bottomPanel.add(removeStudentDormitoryButton);
        bottomPanel.add(changeStudentDormitoryButton);
        studentDormitoryFrame.add(bottomPanel, BorderLayout.SOUTH);

        // 加载学生寝室信息
        loadStudentDormitoryTableData(studentDormitoryTableModel);

        // 按钮事件
        assignDormitoryButton.addActionListener(e -> {
            JTextField snoField = new JTextField();
            JComboBox<String> dormitoryComboBox = new JComboBox<>();
            loadDormitoryDataToComboBox(dormitoryComboBox);

            Object[] inputFields = {
                    "学号:", snoField,
                    "寝室号:", dormitoryComboBox
            };

            int option = JOptionPane.showConfirmDialog(studentDormitoryFrame, inputFields, "分配寝室", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                boolean success = DatabaseHelper.assignStudentDormitory(// 分配寝室
                        snoField.getText(),
                        dormitoryComboBox.getSelectedItem().toString()
                );
                if (success) {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "寝室分配成功！");
                    loadStudentDormitoryTableData(studentDormitoryTableModel);
                } else {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "寝室分配失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        removeStudentDormitoryButton.addActionListener(e -> {
            int selectedRow = studentDormitoryTable.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(studentDormitoryFrame, "请选择要从寝室移除的学生！");
                return;
            }

            String sno = (String) studentDormitoryTableModel.getValueAt(selectedRow, 0);
            int option = JOptionPane.showConfirmDialog(studentDormitoryFrame, "确认移除学号为 " + sno + " 的寝室？", "确认移除", JOptionPane.YES_NO_OPTION);
            if (option == JOptionPane.YES_OPTION) {
                boolean success = DatabaseHelper.removeStudentDormitory(sno);
                if (success) {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "此学生已从寝室移除成功！");
                    loadStudentDormitoryTableData(studentDormitoryTableModel);
                } else {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "移除失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        changeStudentDormitoryButton.addActionListener(e -> {
            JTextField snoField = new JTextField();
            JComboBox<String> dormitoryComboBox = new JComboBox<>();

            // 加载寝室号到下拉框
            loadDormitoryDataToComboBox1(dormitoryComboBox);

            Object[] inputFields = {
                    "学号:", snoField,
                    "寝室号:", dormitoryComboBox
            };

            int option = JOptionPane.showConfirmDialog(studentDormitoryFrame, inputFields, "更换寝室", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String sno = snoField.getText();
                String newDormitory = dormitoryComboBox.getSelectedItem().toString();

                // 验证学号是否存在
                if (!DatabaseHelper.isStudentExist(sno)) {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "学号不存在！", "错误", JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // 更换寝室逻辑
                boolean success = DatabaseHelper.changeStudentDormitory(sno, newDormitory);
                if (success) {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "寝室更换成功！");
                    loadStudentDormitoryTableData(studentDormitoryTableModel); // 重新加载表格数据
                } else {
                    JOptionPane.showMessageDialog(studentDormitoryFrame, "寝室更换失败！", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        studentDormitoryFrame.setVisible(true);
        studentDormitoryFrame.setLocationRelativeTo(null);
    }

    /**
     * 加载学生寝室信息到表格
     */
    private void loadStudentDormitoryTableData(DefaultTableModel studentDormitoryTableModel) {
        studentDormitoryTableModel.setRowCount(0); // 清空表格
        try (ResultSet rs = DatabaseHelper.getAllStudentDormitories()) {
            while (rs.next()) {
                studentDormitoryTableModel.addRow(new Object[]{
                        rs.getString("SNO"),
                        rs.getString("SName"),
                        rs.getString("RNO")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "加载学生寝室数据失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 加载寝室数据到下拉框
     */
    private void loadDormitoryDataToComboBox(JComboBox<String> comboBox) {
        comboBox.removeAllItems(); // 清空下拉框
        try (ResultSet rs = DatabaseHelper.getAllDormitories()) {
            while (rs.next()) {
                comboBox.addItem(rs.getString("RNO"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadDormitoryDataToComboBox1(JComboBox<String> comboBox) {
        comboBox.removeAllItems(); // 清空下拉框
        try (ResultSet rs = DatabaseHelper.getAllDormitories()) {
            while (rs.next()) {
                comboBox.addItem(rs.getString("RNO")); // 添加寝室号
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "加载寝室数据失败！", "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

}
