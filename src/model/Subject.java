package model;

public class Subject {
    private String idmonhoc;
    private String tenmon;

    public Subject(String idmonhoc, String tenmon) {
        this.idmonhoc = idmonhoc;
        this.tenmon = tenmon;
    }

    public String getIdmonhoc() { return idmonhoc; }
    public String getTenmon() { return tenmon; }

    public void setIdmonhoc(String idmonhoc) { this.idmonhoc = idmonhoc; }
    public void setTenmon(String tenmon) { this.tenmon = tenmon; }
}