package control;

import model.CLass;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ClassControl {

    public List<CLass> getAll() {
        List<CLass> list = new ArrayList<>();
        String query = "SELECT * FROM lop";
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new CLass(rs.getString("malop"), rs.getString("tenlop"), rs.getString("idmonhoc")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(CLass CLass) {
        String query = "INSERT INTO lop (malop, tenlop, idmonhoc) VALUES (?, ?, ?)";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, CLass.getMalop());
            stmt.setString(2, CLass.getTenlop());
            stmt.setString(3, CLass.getIdmonhoc());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(CLass CLass) {
        String query = "UPDATE lop SET tenlop=? WHERE malop=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, CLass.getTenlop());
            stmt.setString(2, CLass.getMalop());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String malop) {
        String query = "DELETE FROM lop WHERE malop=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, malop);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    // Thêm hàm này vào ClassControl.java
    public List<CLass> getByMonHoc(String idmonhoc) {
        List<CLass> list = new ArrayList<>();
        String query = "SELECT * FROM lop WHERE idmonhoc = ?";

        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, idmonhoc);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                CLass lop = new CLass(
                        rs.getString("malop"),
                        rs.getString("tenlop"),
                        rs.getString("idmonhoc")
                );
                list.add(lop);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm lấy danh sách Lớp học dựa vào Mã môn học
    public List<String> getClassesBySubject(String idMonHoc) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT malop, tenlop FROM lop WHERE idmonhoc = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, idMonHoc);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("malop") + " - " + rs.getString("tenlop"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    // Hàm lấy danh sách các lớp mà sinh viên ĐÃ THAM GIA (Dùng cho Phòng Chat)
    public List<String> getEnrolledClasses(String masv) {
        List<String> list = new ArrayList<>();
        String sql = "SELECT l.malop, l.tenlop FROM lop l JOIN diem d ON l.idmonhoc = d.idmonhoc WHERE d.masv = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql)) {

            pst.setString(1, masv);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                list.add(rs.getString("malop") + " - " + rs.getString("tenlop"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}