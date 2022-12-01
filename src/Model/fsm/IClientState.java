package Model.fsm;

import java.util.ArrayList;

public interface IClientState {
    ClientState getState();
    boolean regista(ArrayList<String> temp);
    boolean login(ArrayList<String> temp);
}
