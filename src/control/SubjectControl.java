package control;

import model.Subject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SubjectControl {

    public List<Subject> getAll() {
        List<Subject> list = new ArrayList<>();
        String query = "SELECT * FROM monhoc";
        try (Connection conn = Databaseconnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                list.add(new Subject(rs.getString("idmonhoc"), rs.getString("tenmon"), rs.getInt("sotinchi")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean add(Subject mh) {
        String query = "INSERT INTO monhoc (idmonhoc, tenmon, sotinchi) VALUES (?, ?, ?)";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, mh.getIdmonhoc());
            stmt.setString(2, mh.getTenmon());
            stmt.setInt(3,mh.getSotinchi());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Subject mh) {
        String query = "UPDATE monhoc SET tenmon=? WHERE idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, mh.getTenmon());
            stmt.setString(2, mh.getIdmonhoc());
            stmt.setInt(3,mh.getSotinchi());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean delete(String idSubject) {
        String query = "DELETE FROM monhoc WHERE idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idSubject);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public Subject getById(String idmonhoc) {
        String query = "SELECT * FROM monhoc WHERE idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idmonhoc);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new Subject(rs.getString("idmonhoc"), rs.getString("tenmon"), rs.getInt("sotinchi"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    // Hàm lấy tất cả môn học hiển thị cho ComboBox
    public List<String> getAllSubjectNames() {
        List<String> list = new ArrayList<>();
        String sql = "SELECT idmonhoc, tenmon FROM monhoc";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement pst = conn.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                list.add(rs.getString("idmonhoc") + " - " + rs.getString("tenmon"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}