package model;

import java.time.LocalDate;

public class Attendance {
    private int id;
    private String masv, idmonhoc, malop, trangthai;
    private LocalDate ngay;

    public Attendance(int id, String masv, String idmonhoc, String malop, LocalDate ngay, String trangthai) {
        this.id = id;
        this.masv = masv;
        this.idmonhoc = idmonhoc;
        this.malop = malop;
        this.ngay = ngay;
        this.trangthai = trangthai;
    }

    public int getId() { return id; }
    public String getMasv() { return masv; }
    public String getIdmonhoc() { return idmonhoc; }
    public String getMalop() { return malop; }
    public LocalDate getNgay() { return ngay; }
    public String getTrangthai() { return trangthai; }
    public void setTrangthai(String trangthai) { this.trangthai = trangthai; }
}