package model;

public class CLass {
    private String malop;
    private String tenlop;
    private String idmonhoc;

    public CLass(String malop, String tenlop, String idmonhoc) {
        this.malop = malop;
        this.tenlop = tenlop;
        this.idmonhoc = idmonhoc;
    }

    public String getMalop() { return malop; }
    public String getTenlop() { return tenlop; }
    public String getIdmonhoc(){ return idmonhoc; }

    public void setMalop(String malop) { this.malop = malop; }
    public void setTenlop(String tenlop) { this.tenlop = tenlop; }
    public void setIdmonhoc(String idmonhoc) { this.idmonhoc = idmonhoc; }
}