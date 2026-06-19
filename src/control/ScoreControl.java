package control;

import model.Score;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScoreControl {

    private Score fromRs(ResultSet rs) throws SQLException {
        return new Score(
                rs.getString("masv"),
                rs.getString("idmonhoc"),
                rs.getDouble("chuyencan"),
                rs.getDouble("baitap"),
                rs.getDouble("giuaki"),
                rs.getDouble("cuoiki")
        );
    }

    public List<Score> getByMonAndLop(String idmonhoc, String malop) {
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
            while (rs.next()) list.add(fromRs(rs));
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public Score getByMasvAndMon(String masv, String idmonhoc) {
        String query = "SELECT * FROM diem WHERE masv=? AND idmonhoc=?";
        try (Connection conn = Databaseconnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, masv);
            stmt.setString(2, idmonhoc);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return fromRs(rs);
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

    // Tự động thêm mới hoặc cập nhật
    public boolean save(Score d) {
        if (getByMasvAndMon(d.getMasv(), d.getIdmonhoc()) != null) {
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
}