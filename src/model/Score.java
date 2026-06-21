package model;

public class Score {
    private String masv;
    private String idmonhoc;
    private double chuyenCan;
    private double baiTap;
    private double giuaKi;
    private double cuoiKi;
    private double diemTong;
    private int sotinchi;

    public Score(String masv, String idmonhoc, double chuyenCan, double baiTap, double giuaKi, double cuoiKi, int sotinchi) {
        this.masv      = masv;
        this.idmonhoc  = idmonhoc;
        this.chuyenCan = chuyenCan;
        this.baiTap    = baiTap;
        this.giuaKi    = giuaKi;
        this.cuoiKi    = cuoiKi;
        this.sotinchi = sotinchi;
        this.diemTong  = tinhTong(chuyenCan, baiTap, giuaKi, cuoiKi,sotinchi);
    }

    public static double tinhTong(double cc, double bt, double gk, double ck, int soTC) {
        double tong = 0;
        if (soTC == 3) {
            tong = cc * 0.1 + bt * 0.2 + gk * 0.2 + ck * 0.5;
        } else if (soTC == 2) {
            tong = cc * 0.1 + gk * 0.3 + ck * 0.6; // Bỏ qua Bài tập
        } else if (soTC == 1) {
            tong = cc * 0.4 + ck * 0.6; // Bỏ qua CC và Bài tập
        }
        return Math.round(tong * 10.0) / 10.0;
    }

    public String getMasv()      { return masv; }
    public String getIdmonhoc()  { return idmonhoc; }
    public double getChuyenCan() { return chuyenCan; }
    public double getBaiTap()    { return baiTap; }
    public double getGiuaKi()    { return giuaKi; }
    public double getCuoiKi()    { return cuoiKi; }
    public double getDiemTong()  { return diemTong; }
    public int getsotinchi()  { return sotinchi;}

    public void setMasv(String masv)         { this.masv = masv; }
    public void setIdmonhoc(String idmonhoc) { this.idmonhoc = idmonhoc; }
    public void setChuyenCan(double v)       { this.chuyenCan = v; recalc(); }
    public void setBaiTap(double v)          { this.baiTap = v; recalc(); }
    public void setGiuaKi(double v)          { this.giuaKi = v; recalc(); }
    public void setCuoiKi(double v)          { this.cuoiKi = v; recalc(); }

    public void recalc() {
        this.diemTong = tinhTong(chuyenCan, baiTap, giuaKi, cuoiKi, sotinchi);
    }
}