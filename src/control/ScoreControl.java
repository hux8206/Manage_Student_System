package control;

import model.Score;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreControl {

    private Score fromRs(ResultSet rs, int sotinchi) throws SQLException {
        return new Score(
                rs.getString("masv"),
                rs.getString("idmonhoc"),
                rs.getDouble("chuyencan"),
                rs.getDouble("baitap"),
                rs.getDouble("giuaki"),
                rs.getDouble("cuoiki"),
                sotinchi
        );
    }

    public List<Score> getByMonAndLop(String idmonhoc, String malop, int sotinchi) {
        List<Score> list = new ArrayList<>();
        String query =
                "SELECT d.* FROM diem d " +
                        "JOIN sinhvien sv ON d.masv = sv.masv " +
                        "WHERE d.idmonhoc = ? AND sv.malop = ?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, idmonhoc);
            stmt.setString(2, malop);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) list.add(fromRs(rs, sotinchi));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Score getByMasvAndMon(String masv, String idmonhoc, int sotinchi) {
        String query = "SELECT * FROM diem WHERE masv=? AND idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, masv);
            stmt.setString(2, idmonhoc);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return fromRs(rs, sotinchi);
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    public boolean add(Score d) {
        String query = "INSERT INTO diem (masv, idmonhoc, chuyencan, baitap, giuaki, cuoiki, diemtong) VALUES (?,?,?,?,?,?,?)";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, d.getMasv());
            stmt.setString(2, d.getIdmonhoc());
            stmt.setDouble(3, d.getChuyenCan());
            stmt.setDouble(4, d.getBaiTap());
            stmt.setDouble(5, d.getGiuaKi());
            stmt.setDouble(6, d.getCuoiKi());
            stmt.setDouble(7, d.getDiemTong());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean update(Score d) {
        String query = "UPDATE diem SET chuyencan=?, baitap=?, giuaki=?, cuoiki=?, diemtong=? WHERE masv=? AND idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setDouble(1, d.getChuyenCan());
            stmt.setDouble(2, d.getBaiTap());
            stmt.setDouble(3, d.getGiuaKi());
            stmt.setDouble(4, d.getCuoiKi());
            stmt.setDouble(5, d.getDiemTong());
            stmt.setString(6, d.getMasv());
            stmt.setString(7, d.getIdmonhoc());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean save(Score d) {
        if (getByMasvAndMon(d.getMasv(), d.getIdmonhoc(), d.getsotinchi()) != null) {
            return update(d);
        }
        return add(d);
    }

    public boolean delete(String masv, String idmonhoc) {
        String query = "DELETE FROM diem WHERE masv=? AND idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, masv);
            stmt.setString(2, idmonhoc);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) { e.printStackTrace(); return false; }
    }

    public boolean enrollStudent(String masv, String idMonHoc, String maLop) {
        try (Connection conn = Databaseconnection.getConnection()) {

            // --- 1. CẬP NHẬT BẢNG SINH VIÊN (Để Giáo viên thấy) ---
            String checkStudentSql = "SELECT masv FROM sinhvien WHERE masv = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkStudentSql)) {
                checkStmt.setString(1, masv);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    // Nếu sinh viên chưa tồn tại -> Thêm mới kèm Mã lớp
                    String insertStudentSql = "INSERT INTO sinhvien (masv, ten, malop) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertStudentSql)) {
                        insertStmt.setString(1, masv);
                        insertStmt.setString(2, "Sinh viên " + masv);
                        insertStmt.setString(3, maLop);
                        insertStmt.executeUpdate();
                    }
                } else {
                    // Nếu sinh viên đã tồn tại -> Cập nhật lại Mã lớp
                    String updateStudentSql = "UPDATE sinhvien SET malop = ? WHERE masv = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateStudentSql)) {
                        updateStmt.setString(1, maLop);
                        updateStmt.setString(2, masv);
                        updateStmt.executeUpdate();
                    }
                }
            }

            // --- 2. THÊM MÔN HỌC VÀO BẢNG ĐIỂM (Để Sinh viên thấy) ---
            String sql = "INSERT INTO diem (masv, idmonhoc, chuyencan, baitap, giuaki, cuoiki, diemtong) VALUES (?, ?, 10, 0, 0, 0, 0)";
            try (PreparedStatement pst = conn.prepareStatement(sql)) {
                pst.setString(1, masv);
                pst.setString(2, idMonHoc);
                return pst.executeUpdate() > 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
