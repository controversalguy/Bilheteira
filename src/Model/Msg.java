package Model;

import java.io.Serial;
import java.io.Serializable;

public class Msg implements Serializable {
    @Serial
    static final long serialVersionUID = 1L;
    protected String msg;

    public Msg(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
