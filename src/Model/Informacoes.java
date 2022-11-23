package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Objects;

public class Informacoes implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;

    protected Integer porto;
    protected String ip;
    protected int ligacoes;
    protected float versaoBd;

    protected String currentTime;

    public Informacoes(Integer porto, String ip, int ligacoes) {
        this.porto = porto;
        this.ip = ip;
        this.ligacoes = ligacoes;
    }

    public Informacoes(Integer porto, String ip, int ligacoes, String currentTime) {
        this.porto = porto;
        this.ip = ip;
        this.ligacoes = ligacoes;
        this.currentTime = currentTime;
    }

    public Informacoes(Integer porto, String ip, int ligacoes, float versaoBd) {
        this.porto = porto;
        this.ip = ip;
        this.ligacoes = ligacoes;
        this.versaoBd = versaoBd;
    }

    public Integer getPorto() {
        return porto;
    }

    public void setPorto(Integer porto) {
        this.porto = porto;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getLigacoes() {
        return ligacoes;
    }

    public void setLigacoes(int ligacoes) {
        this.ligacoes = ligacoes;
    }

    public float getVersaoBd() {
        return versaoBd;
    }

    public void setVersaoBd(float versaoBd) {
        this.versaoBd = versaoBd;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    @Override
    public String toString() {
        return "Porto " + porto +" Ip: "+ ip +" LigacoesTCP: "+ ligacoes + " Hora: "+ currentTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Informacoes)) return false;
        Informacoes info = (Informacoes) o;
        return Objects.equals(porto, info.getPorto());
    }

    @Override
    public int hashCode() {
        return Objects.hash(porto);
    }
}
