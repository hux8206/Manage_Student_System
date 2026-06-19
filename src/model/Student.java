package model;

public class Student {
    private String masv;
    private String ten;
    private String malop;

    public Student(String masv, String ten, String malop) {
        this.masv = masv;
        this.ten = ten;
        this.malop = malop;
    }

    public String getMasv() { return masv; }
    public String getTen() { return ten; }
    public String getMalop() { return malop; }

    public void setMasv(String masv) { this.masv = masv; }
    public void setTen(String ten) { this.ten = ten; }
    public void setMalop(String malop) { this.malop = malop; }
}
