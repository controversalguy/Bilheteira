package Model;

import java.io.Serial;
import java.io.Serializable;

public class Msg implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;

    protected String msg;
    protected int portoServer;
    protected boolean disponibilidade;
    protected float dbVersao;
    protected int nTCPAtivas;
    protected String ip;
    protected boolean lastPort;
    protected int ligacoesTCP;

    public Msg(){ }

    public Msg(String ipServer , Integer portoServer){
        this.ip = ipServer;
        this.portoServer = portoServer;
    }

    public byte[] getMsgBuffer() {
        return msgBuffer;
    }

    public void setMsgBuffer(byte[] msgBuffer) {
        this.msgBuffer = msgBuffer;
    }

    public int getMsgSize() {
        return msgSize;
    }

    public void setMsgSize(int msgSize) {
        this.msgSize = msgSize;
    }

    public void setLastPacket(boolean lastPacket) {
        this.lastPacket = lastPacket;
    }
    public boolean isLastPacket() {
        return lastPacket;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public void setPortoServer(int portoServer) {
        this.portoServer = portoServer;
    }
    public Integer getPortoServer() {
        return this.portoServer;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }
    public String getIp() {
        return ip;
    }

    public int getLigacoesTCP() {
        return ligacoesTCP;
    }

    public void setLigacoesTCP(int ligacoesTCP) {
        this.ligacoesTCP = ligacoesTCP;
    }
}
