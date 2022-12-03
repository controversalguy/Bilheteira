package Model.fsm;

import java.util.ArrayList;

public interface IClientState {
    ClientState getState();
    boolean regista(ArrayList<String> temp);
    boolean login(ArrayList<String> temp);
    boolean edita(ArrayList<String> temp);
    void avancar();
    void regressar();
    boolean inserirEspetaculo(ArrayList<String> temp);

    boolean visivel(ArrayList<String> temp);

    boolean consulta(ArrayList<String> temp);
}
