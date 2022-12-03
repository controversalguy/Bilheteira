package Model.fsm;

import java.util.ArrayList;

public interface IClientState {
    ClientState getState();
    boolean regista(ArrayList<String> temp);
    boolean login(ArrayList<String> temp);
    boolean edita(ArrayList<String> temp);
    void avancar(int i);
    void regressar();
    boolean inserirEspetaculo(ArrayList<String> temp);

    boolean visivel(ArrayList<String> temp);

    boolean consulta(ArrayList<String> temp);
    boolean selecionar(ArrayList<String> temp);
}
