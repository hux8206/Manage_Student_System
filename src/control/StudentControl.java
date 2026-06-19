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
}