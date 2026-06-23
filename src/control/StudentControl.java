package control;

import model.Student;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentControl {

    public List<Student> getAll() {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM sinhvien";
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("masv"),
                        rs.getString("ten"),
                        rs.getString("malop")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Lấy sinh viên theo lớp
    public List<Student> getByLop(String malop) {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM sinhvien WHERE malop = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, malop);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("masv"),
                        rs.getString("ten"),
                        rs.getString("malop")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Student sv) {
        String query = "INSERT INTO sinhvien (masv, ten, malop) VALUES (?, ?, ?)";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sv.getMasv());
            stmt.setString(2, sv.getTen());
            stmt.setString(3, sv.getMalop());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Student sv) {
        String query = "UPDATE sinhvien SET ten=?, malop=? WHERE masv=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, sv.getTen());
            stmt.setString(2, sv.getMalop());
            stmt.setString(3, sv.getMasv());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String masv) {
        String query = "DELETE FROM sinhvien WHERE masv=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, masv);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public List<Student> search(String keyword) {
        List<Student> list = new ArrayList<>();
        String query = "SELECT * FROM sinhvien WHERE LOWER(masv) LIKE ? OR LOWER(ten) LIKE ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, "%" + keyword + "%");
            stmt.setString(2, "%" + keyword + "%");
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Student(
                        rs.getString("masv"),
                        rs.getString("ten"),
                        rs.getString("malop")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    // Hàm lấy họ tên sinh viên (nếu đã có trong CSDL)
    public String getStudentName(String masv) {
        String sql = "SELECT ten FROM sinhvien WHERE masv = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {
            pst.setString(1, masv);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return rs.getString("ten");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ""; // Trả về chuỗi rỗng nếu chưa có
    }

    // Hàm thêm mới hoặc cập nhật thông tin sinh viên
    public boolean updateStudentInfo(String masv, String newName) {
        try (Connection conn = Databaseconnection.getConnection()) {
            // Kiểm tra xem sinh viên này đã có trong bảng sinhvien chưa
            String checkSql = "SELECT masv FROM sinhvien WHERE masv = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, masv);
                ResultSet rs = checkStmt.executeQuery();

                if (rs.next()) {
                    // Nếu đã có -> UPDATE tên mới
                    String updateSql = "UPDATE sinhvien SET ten = ? WHERE masv = ?";
                    try (PreparedStatement upStmt = conn.prepareStatement(updateSql)) {
                        upStmt.setString(1, newName);
                        upStmt.setString(2, masv);
                        return upStmt.executeUpdate() > 0;
                    }
                } else {
                    // Nếu chưa có (Lần đầu đăng nhập) -> INSERT mới
                    String insertSql = "INSERT INTO sinhvien (masv, ten) VALUES (?, ?)";
                    try (PreparedStatement inStmt = conn.prepareStatement(insertSql)) {
                        inStmt.setString(1, masv);
                        inStmt.setString(2, newName);
                        return inStmt.executeUpdate() > 0;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}