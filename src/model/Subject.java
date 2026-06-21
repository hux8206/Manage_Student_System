package model;

public class Subject {
    private String idmonhoc;
    private String tenmon;
    private int sotinchi;

    public Subject(String idmonhoc, String tenmon, int sotinchi) {
        this.idmonhoc = idmonhoc;
        this.tenmon = tenmon;
        this.sotinchi = sotinchi;
    }

    public String getIdmonhoc() { return idmonhoc; }
    public String getTenmon() { return tenmon; }
    public int getSotinchi() { return sotinchi;}

    public void setIdmonhoc(String idmonhoc) { this.idmonhoc = idmonhoc; }
    public void setTenmon(String tenmon) { this.tenmon = tenmon; }
    public void setSotinchi(int sotinchi) {this.sotinchi = sotinchi;}
}