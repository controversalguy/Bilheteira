package Model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public class Msg implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;

    protected String msg;
    protected Integer portoTCP;

    public Msg(String msg, int porto) {
        this.msg = msg;
        this.portoTCP = porto;
    }

    public String getMsg() {
        return msg;
    }
    public void setMsg(String msg) {
        this.msg = msg;
    }
    public int getPortoTCP() {
        return portoTCP;
    }
}
