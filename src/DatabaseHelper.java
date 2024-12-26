import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper {
    public static Connection getConnection() {
        try {
            return DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=dormitory_management;user=sa;password=abc;encrypt=false");
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean login(String role, String username, String password) {
        String sql = switch (role) {
            case "管理员" -> "SELECT * FROM 管理员 WHERE MNO = ? AND MPassword = ?";
            case "学生" -> "SELECT * FROM 学生 WHERE SNO = ? AND SPassword = ?";
            default -> null;
        };

        if (sql == null) return false;

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();//执行查询
            return rs.next();//如果有下一行则返回true
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public static boolean registerVisitor(String username, String password) {
        String sql = "INSERT INTO 来访人信息 (VName, VPassword) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 访客登记来访
     */
    public static boolean registerVisit(String id, String sno, String reason, java.sql.Timestamp startTime, java.sql.Timestamp endTime) {
        String sql = "INSERT INTO 来访登记 (VSFZH, SNO, VReason, VTime, VLeave, VNO) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, sno);
            stmt.setString(3, reason);
            stmt.setTimestamp(4, startTime); // 使用 setTimestamp 方法
            stmt.setTimestamp(5, endTime);  // 使用 setTimestamp 方法
            // 生成来访编号,保证唯一性，取已有编号最大值加1
            String getMaxVNOQuery = "SELECT MAX(VNO) AS MaxVNO FROM 来访登记";
            ResultSet rs = conn.createStatement().executeQuery(getMaxVNOQuery);
            String nextVNO = "000001"; // 默认从 00001 开始
            if (rs.next() && rs.getString("MaxVNO") != null) {
                String maxVNO = rs.getString("MaxVNO"); // 获取最大编号
                int maxVNOInt = Integer.parseInt(maxVNO); // 转换为整数
                nextVNO = String.format("%06d", maxVNOInt + 1); // 格式化为6位
            }
            stmt.setString(6, nextVNO);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static Timestamp getVisitorLeaveTime(String visitorId) {
        String sql = "SELECT VLeave FROM 来访登记 WHERE VSFZH = ? AND VLeave > GETDATE()";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitorId);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getTimestamp("VLeave") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean extendVisitorLeaveTime(String visitorId, double additionalHours) {
        String sql = "UPDATE 来访登记 SET VLeave = DATEADD(HOUR, ?, VLeave) WHERE VSFZH = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, additionalHours);
            stmt.setString(2, visitorId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static List<Object[]> getVisitorHistory(String visitorId) {
        String sql = "SELECT VNO, SNO, VReason, VTime, VLeave FROM 来访登记 WHERE VSFZH = ?";
        List<Object[]> historyData = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitorId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    historyData.add(new Object[]{
                            rs.getString("VNO"),
                            rs.getString("SNO"),
                            rs.getString("VReason"),
                            rs.getString("VTime"),
                            rs.getString("VLeave")
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return historyData;
    }





    /**
     * 获取学生信息
     */
    public static ResultSet getStudentInfo(String studentSNO) {
        String sql = "SELECT * FROM 学生 WHERE SNO = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, studentSNO);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }


    public static boolean registerRepair(String roomNumber, String detail) {
        String getMaxFNOQuery = "SELECT MAX(FNO) AS MaxFNO FROM 报修信息";//查询最大编号
        String insertQuery = "INSERT INTO 报修信息 (FDetail, FTime, FBool, RNO, FNO) VALUES (?, GETDATE(), 0, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement getMaxStmt = conn.prepareStatement(getMaxFNOQuery);
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {

            // 查询当前最大 FNO
            ResultSet rs = getMaxStmt.executeQuery();//执行查询
            String nextFNO = "00001"; // 默认从 00001 开始
            if (rs.next() && rs.getString("MaxFNO") != null) {
                String maxFNO = rs.getString("MaxFNO"); // 获取最大编号
                int maxFNOInt = Integer.parseInt(maxFNO); // 转换为整数
                nextFNO = String.format("%05d", maxFNOInt + 1); // 格式化为5位
            }

            // 插入新报修信息
            insertStmt.setString(1, detail);
            insertStmt.setString(2, roomNumber);
            insertStmt.setString(3, nextFNO);
            return insertStmt.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }



    public static ResultSet getRoomStatus(String roomNumber) {
        String sql = "SELECT 学生.SNO, 学生.SName, 学生.SSex, 学生.SPhone, 学生.SCollege, 学生.SClass " +
                "FROM 学生居住寝室 " +
                "JOIN 学生 ON 学生居住寝室.SNO = 学生.SNO " +
                "WHERE 学生居住寝室.RNO = ?";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);//创建一个PreparedStatement对象,用于将参数传递到SQL语句,并执行SQL语句
            System.out.println(roomNumber);
            stmt.setString(1, roomNumber);//设置参数
            return stmt.executeQuery();//执行查询
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 管理员查看报修信息
     */
    public static ResultSet getRepairInfo() {
        String sql = "SELECT 报修信息.FNO, 报修信息.FDetail, 报修信息.FTime, 报修信息.FBool, 寝室.RNO " +
                "FROM 报修信息 " +
                "JOIN 寝室 ON 报修信息.RNO = 寝室.RNO";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 管理员更新报修状态
     */
    public static boolean updateRepairStatus(String repairID, boolean isResolved) {
        String sql = "UPDATE 报修信息 SET FBool = ? WHERE FNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isResolved);
            stmt.setString(2, repairID);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 管理员查看来访记录
     */
    public static ResultSet getVisitRecords() {
        String sql = "SELECT 来访登记.VNO, 来访登记.VReason, 来访登记.VTime, 来访登记.VLeave, 学生.SName, 来访人信息.VName " +
                "FROM 来访登记 " +
                "JOIN 学生 ON 来访登记.SNO = 学生.SNO " +
                "JOIN 来访人信息 ON 来访登记.VSFZH = 来访人信息.VSFZH";
        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getStudentRoomNumber(String studentSNO) {
        String sql = "SELECT RNO FROM 学生居住寝室 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentSNO);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("RNO") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getRoomInfo(String roomNumber) {
        String sql = "SELECT * FROM 寝室 WHERE RNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getRoomMembers(String roomNumber) {
        String sql = "SELECT 学生.SNO, 学生.SName FROM 学生居住寝室 JOIN 学生 ON 学生居住寝室.SNO = 学生.SNO WHERE RNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static char[] getRoomCapacity(String roomNumber) {
        String sql = "SELECT RMax FROM 寝室 WHERE RNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("RMax").toCharArray() : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ResultSet getAllDormitories() {
        String sql = "SELECT * FROM 寝室";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addDormitory(String dormitoryNumber, int maxCapacity) {
        String sql = "INSERT INTO 寝室 (RNO, RNumber, RMax) VALUES (?, 0, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dormitoryNumber);
            stmt.setInt(2, maxCapacity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateDormitory(String dormitoryNumber, int maxCapacity) {
        String sql = "UPDATE 寝室 SET RMax = ? WHERE RNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, maxCapacity);
            stmt.setString(2, dormitoryNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteDormitory(String dormitoryNumber) {
        String sql = "DELETE FROM 寝室 WHERE RNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, dormitoryNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getAllStudentDormitories() {
        String sql = "SELECT 学生居住寝室.SNO, 学生.SName, 学生居住寝室.RNO " +
                "FROM 学生居住寝室 " +
                "JOIN 学生 ON 学生居住寝室.SNO = 学生.SNO";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean assignStudentDormitory(String sno, String dormitoryNumber) {
        String sql = "INSERT INTO 学生居住寝室 (SNO, RNO) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            stmt.setString(2, dormitoryNumber);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean removeStudentDormitory(String sno) {
        String sql = "DELETE FROM 学生居住寝室 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getStudents(String sno, String name, String college, String sClass, String dormitory) {
        StringBuilder sql = new StringBuilder(
                "SELECT 学生.SNO, 学生.SName, 学生.SSex, 学生.SPhone, 学生.SCollege, 学生.SClass, 学生居住寝室.RNO " +
                        "FROM 学生 " +
                        "LEFT JOIN 学生居住寝室 ON 学生.SNO = 学生居住寝室.SNO " +
                        "WHERE 1=1"
        );

        if (sno != null && !sno.isEmpty()) sql.append(" AND 学生.SNO LIKE ?");
        if (name != null && !name.isEmpty()) sql.append(" AND 学生.SName LIKE ?");
        if (college != null && !college.isEmpty()) sql.append(" AND 学生.SCollege LIKE ?");
        if (sClass != null && !sClass.isEmpty()) sql.append(" AND 学生.SClass LIKE ?");
        if (dormitory != null && !dormitory.isEmpty()) sql.append(" AND 学生居住寝室.RNO = ?");

        try {
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql.toString());

            int index = 1;
            if (sno != null && !sno.isEmpty()) stmt.setString(index++, "%" + sno + "%");
            if (name != null && !name.isEmpty()) stmt.setString(index++, "%" + name + "%");
            if (college != null && !college.isEmpty()) stmt.setString(index++, "%" + college + "%");
            if (sClass != null && !sClass.isEmpty()) stmt.setString(index++, "%" + sClass + "%");
            if (dormitory != null && !dormitory.isEmpty()) stmt.setString(index++, dormitory);

            return stmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean addStudent(String sno, String name, String sex, String phone, String college, String sClass) {
        String sql = "INSERT INTO 学生 (SNO, SName, SSex, SPhone, SCollege, SClass) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            stmt.setString(2, name);
            stmt.setString(3, sex);
            stmt.setString(4, phone);
            stmt.setString(5, college);
            stmt.setString(6, sClass);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean updateStudent(String sno, String name, String sex, String phone, String college, String sClass,String password) {
        String sql = "UPDATE 学生 SET SName = ?, SSex = ?, SPhone = ?, SCollege = ?, SClass = ? , SPassword=? WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, sex);
            stmt.setString(3, phone);
            stmt.setString(4, college);
            stmt.setString(5, sClass);
            stmt.setString(6, password);
            stmt.setString(7, sno);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteStudent(String sno) {
        String sql = "DELETE FROM 学生 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isStudentExist(String text) {
        String sql = "SELECT * FROM 学生 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, text);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean changeStudentDormitory(String sno, String newDormitory) {
        String sql = "UPDATE 学生居住寝室 SET RNO = ? WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newDormitory);
            stmt.setString(2, sno);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static ResultSet getAllRepairs() {
        String sql = "SELECT FNO, RNO, FDetail, FTime, FBool, FRemark FROM 报修信息";
        try {
            Connection conn = getConnection();
            return conn.prepareStatement(sql).executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static boolean markRepairAsResolved(String repairId) {
        String sql = "UPDATE 报修信息 SET FBool = 1 WHERE FNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, repairId);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除报修信息
    public static boolean deleteRepair(String repairId) {
        String sql = "DELETE FROM 报修信息 WHERE FNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, repairId);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean deleteVisitor(String visitorId) {
        String sql = "DELETE FROM 来访人信息 WHERE VSFZH = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitorId);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 删除来访记录
    public static boolean deleteVisitRecord(String visitId) {
        String sql = "DELETE FROM 来访登记 WHERE VNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, visitId);
            //更新来访登记表的编号，并保证编号是6位
            String getMaxVNOQuery = "SELECT MAX(VNO) AS MaxVNO FROM 来访登记";
            ResultSet rs = conn.createStatement().executeQuery(getMaxVNOQuery);
            String nextVNO = "000001"; // 默认从 00001 开始
            if (rs.next() && rs.getString("MaxVNO") != null) {
                String maxVNO = rs.getString("MaxVNO"); // 获取最大编号
                int maxVNOInt = Integer.parseInt(maxVNO); // 转换为整数
                nextVNO = String.format("%06d", maxVNOInt + 1); // 格式化为6位
            }
            String updateVNOQuery = "UPDATE 来访登记 SET VNO = ? WHERE VNO = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateVNOQuery);
            updateStmt.setString(1, visitId);
            updateStmt.setString(2, nextVNO);
            updateStmt.executeUpdate();
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isVisitorExists(String id, String phone) {
        String sql = "SELECT COUNT(*) AS count FROM 来访人信息 WHERE VSFZH = ? AND VPhone = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, phone);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count") > 0; // 如果存在记录，返回 true
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // 默认返回 false
    }

    public static boolean registerVisitor(String id, String phone, String name) {
        String sql = "INSERT INTO 来访人信息 (VSFZH, VPhone, VName) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.setString(2, phone);
            stmt.setString(3, name);
            return stmt.executeUpdate() > 0; // 返回受影响的行数
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }


    public static String getVisitorName(String id) {
        String sql = "SELECT VName FROM 来访人信息 WHERE VSFZH = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("VName") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    //提前结束访问
    public static void endVisitorVisit(String id) {
        // 更新数据库中的离校时间
        String sql = "UPDATE 来访登记 SET VLeave = GETDATE() WHERE VSFZH = ? AND VLeave > GETDATE()";//更新离校时间
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, id);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Object getStudentName(String sno) {
        String sql = "SELECT SName FROM 学生 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("SName") : null;
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static java.util.List<String[]> searchVisitors(String condition) {
        java.util.List<String[]> visitorList = new ArrayList<>();
        String sql = (condition == null || condition.isEmpty())
                ? "SELECT * FROM 来访人信息"
                : "SELECT * FROM 来访人信息 WHERE VName LIKE ? OR VSFZH LIKE ? OR VPhone LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (condition != null && !condition.isEmpty()) {
                stmt.setString(1, "%" + condition + "%");
                stmt.setString(2, "%" + condition + "%");
                stmt.setString(3, "%" + condition + "%");
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitorList.add(new String[]{
                            rs.getString("VSFZH"), // 身份证号
                            rs.getString("VName"), // 姓名
                            rs.getString("VPhone") // 电话
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visitorList;
    }

    //在视图中查询来访记录
    public static java.util.List<String[]> searchVisits(String condition) {
        java.util.List<String[]> visitList = new ArrayList<>();
        String sql = (condition == null || condition.isEmpty())
                ? "SELECT * FROM View_Visit_Record"
                : "SELECT * FROM View_Visit_Record WHERE 学生学号 LIKE ? OR 学生姓名 LIKE ? OR 来访人电话 LIKE ? OR 身份证号 LIKE ? OR 来访人姓名 LIKE ? OR 来访原因 LIKE ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            if (condition != null && !condition.isEmpty()) {
                stmt.setString(1, "%" + condition + "%");
                stmt.setString(2, "%" + condition + "%");
                stmt.setString(3, "%" + condition + "%");
                stmt.setString(4, "%" + condition + "%");
                stmt.setString(5, "%" + condition + "%");
                stmt.setString(6, "%" + condition + "%");
            }

            // 调试日志
            System.out.println("执行的SQL语句：" + stmt);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    visitList.add(new String[]{
                            rs.getString("来访编号"), // 登记编号
                            rs.getString("来访人姓名"), // 访客姓名
                            rs.getString("身份证号"), // 访客身份证号
                            rs.getString("学生学号"), // 被访学号
                            rs.getString("学生姓名"), // 被访学生姓名
                            rs.getString("来访时间"), // 来访时间
                            rs.getString("离开时间"), // 离开时间
                            rs.getString("来访原因") // 来访原因
                    });
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return visitList;
    }

    public static String getStudentPassword(String sno) {
        String sql = "SELECT SPassword FROM 学生 WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, sno);
            ResultSet rs = stmt.executeQuery();
            return rs.next() ? rs.getString("SPassword") : "wrong";
        } catch (SQLException e) {
            e.printStackTrace();
            return "wrong";
        }
    }

    public static boolean updateStudentPassword(String studentSNO, String newPassword) {
        String sql = "UPDATE 学生 SET SPassword = ? WHERE SNO = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, newPassword);
            stmt.setString(2, studentSNO);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}
