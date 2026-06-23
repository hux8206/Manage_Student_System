package control;

import model.Attendance;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AttendanceControl {

    private double getDiemValue(String status) {
        if (status == null) return 0.0;
        if (status.contains("Vắng phép") || status.contains("Vắng có phép")) return -0.5;
        if (status.contains("Vắng")) return -1.0;
        return 0.0;
    }

    public boolean save(Attendance dd) {
        String checkQuery = "SELECT id, trangthai FROM diemdanh WHERE masv=? AND idmonhoc=? AND malop=? AND ngay=?";
        try (Connection conn = Databaseconnection.getConnection()) {
            conn.setAutoCommit(false);

            PreparedStatement check = conn.prepareStatement(checkQuery);
            check.setString(1, dd.getMasv());
            check.setString(2, dd.getIdmonhoc());
            check.setString(3, dd.getMalop());
            check.setDate(4, Date.valueOf(dd.getNgay()));
            ResultSet rs = check.executeQuery();

            double diemThayDoi = 0;

            if (rs.next()) {
                int id = rs.getInt("id");
                String oldStatus = rs.getString("trangthai");

                if (!oldStatus.equals(dd.getTrangthai())) {
                    diemThayDoi = getDiemValue(dd.getTrangthai()) - getDiemValue(oldStatus);
                    // Đã bỏ cột noidung khỏi Update
                    String updateQuery = "UPDATE diemdanh SET trangthai=? WHERE id=?";
                    PreparedStatement update = conn.prepareStatement(updateQuery);
                    update.setString(1, dd.getTrangthai());
                    update.setInt(2, id);
                    update.executeUpdate();
                }
            } else {
                diemThayDoi = getDiemValue(dd.getTrangthai());
                // Đã bỏ cột noidung khỏi Insert
                String insertQuery = "INSERT INTO diemdanh (masv, idmonhoc, malop, ngay, trangthai) VALUES (?,?,?,?,?)";
                PreparedStatement insert = conn.prepareStatement(insertQuery);
                insert.setString(1, dd.getMasv());
                insert.setString(2, dd.getIdmonhoc());
                insert.setString(3, dd.getMalop());
                insert.setDate(4, Date.valueOf(dd.getNgay()));
                insert.setString(5, dd.getTrangthai());
                insert.executeUpdate();
            }

            if (diemThayDoi != 0) {
                String updateDiem = "UPDATE diem SET chuyencan = GREATEST(chuyencan + ?, 0) WHERE masv=? AND idmonhoc=?";
                PreparedStatement psDiem = conn.prepareStatement(updateDiem);
                psDiem.setDouble(1, diemThayDoi);
                psDiem.setString(2, dd.getMasv());
                psDiem.setString(3, dd.getIdmonhoc());
                psDiem.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean saveAll(List<Attendance> list) {
        for (Attendance dd : list) {
            if (!save(dd)) return false;
        }
        return true;
    }

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
                        rs.getString("trangthai") // Bỏ noidung
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public int getSoVang(String masv, String idmonhoc, String malop) {
        String query = "SELECT COUNT(*) FROM diemdanh WHERE masv=? AND idmonhoc=? AND malop=? AND trangthai LIKE '%Vắng%'";
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