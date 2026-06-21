package control;

import model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceControl {

    public List<Attendance> getByNgay(String idmonhoc, String malop, LocalDate ngay) {
        List<Attendance> list = new ArrayList<>();
        String query = "SELECT * FROM diemdanh WHERE idmonhoc=? AND malop=? AND ngay=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idmonhoc);
            stmt.setString(2, malop);
            stmt.setDate(3, Date.valueOf(ngay));
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Attendance(
                        rs.getInt("id"),
                        rs.getString("masv"),
                        rs.getString("idmonhoc"),
                        rs.getString("malop"),
                        rs.getDate("ngay").toLocalDate(),
                        rs.getString("trangthai")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public boolean save(Attendance dd) {
        String checkQuery = "SELECT id FROM diemdanh WHERE masv=? AND idmonhoc=? AND malop=? AND ngay=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement check = conn.prepareStatement(checkQuery)) {
            check.setString(1, dd.getMasv());
            check.setString(2, dd.getIdmonhoc());
            check.setString(3, dd.getMalop());
            check.setDate(4, Date.valueOf(dd.getNgay()));
            ResultSet rs = check.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String updateQuery = "UPDATE diemdanh SET trangthai=? WHERE id=?";
                PreparedStatement update = conn.prepareStatement(updateQuery);
                update.setString(1, dd.getTrangthai());
                update.setInt(2, id);
                return update.executeUpdate() > 0;
            } else {
                String insertQuery = "INSERT INTO diemdanh (masv, idmonhoc, malop, ngay, trangthai) VALUES (?,?,?,?,?)";
                PreparedStatement insert = conn.prepareStatement(insertQuery);
                insert.setString(1, dd.getMasv());
                insert.setString(2, dd.getIdmonhoc());
                insert.setString(3, dd.getMalop());
                insert.setDate(4, Date.valueOf(dd.getNgay()));
                insert.setString(5, dd.getTrangthai());
                return insert.executeUpdate() > 0;
            }
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean saveAll(List<Attendance> list) {
        for (Attendance dd : list) {
            if (!save(dd)) return false;
        }
        return true;
    }

    public int getSoVang(String masv, String idmonhoc, String malop) {
        String query = "SELECT COUNT(*) FROM diemdanh WHERE masv=? AND idmonhoc=? AND malop=? AND trangthai='Vắng'";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, masv);
            stmt.setString(2, idmonhoc);
            stmt.setString(3, malop);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public List<LocalDate> getNgayDaDiemDanh(String idmonhoc, String malop) {
        List<LocalDate> list = new ArrayList<>();
        String query = "SELECT DISTINCT ngay FROM diemdanh WHERE idmonhoc=? AND malop=? ORDER BY ngay DESC";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idmonhoc);
            stmt.setString(2, malop);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(rs.getDate("ngay").toLocalDate());
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }
}