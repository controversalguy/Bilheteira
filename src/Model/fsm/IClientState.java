package Model.fsm;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

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
    boolean consultaReservasPagas(ArrayList<String> temp);
    boolean consultaReservasParaPagamento(ArrayList<String> temp);
    boolean esperaPagamento(AtomicInteger pagamento);

    boolean submeteReserva(ArrayList<String> temp);

    boolean efetuaPagamento(ArrayList<String> temp);
}
