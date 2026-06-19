package model;

import java.time.LocalDate;

public class Attendance {
    private int id;
    private String masv;
    private String idmonhoc;
    private String malop;
    private LocalDate ngay;
    private String trangthai; // "Có mặt", "Vắng", "Vắng có phép"

    public Attendance(int id, String masv, String idmonhoc, String malop, LocalDate ngay, String trangthai) {
        this.id        = id;
        this.masv      = masv;
        this.idmonhoc  = idmonhoc;
        this.malop     = malop;
        this.ngay      = ngay;
        this.trangthai = trangthai;
    }

    public int getId()           { return id; }
    public String getMasv()      { return masv; }
    public String getIdmonhoc()  { return idmonhoc; }
    public String getMalop()     { return malop; }
    public LocalDate getNgay()   { return ngay; }
    public String getTrangthai() { return trangthai; }

    public void setId(int id)                { this.id = id; }
    public void setMasv(String masv)         { this.masv = masv; }
    public void setIdmonhoc(String idmonhoc) { this.idmonhoc = idmonhoc; }
    public void setMalop(String malop)       { this.malop = malop; }
    public void setNgay(LocalDate ngay)      { this.ngay = ngay; }
    public void setTrangthai(String tt)      { this.trangthai = tt; }
}