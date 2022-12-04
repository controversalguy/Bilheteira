package Model.fsm;

import Model.Servidor.Cliente;
import Model.data.ClientData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

abstract class ClientAdapter implements IClientState {
    ClientContext context;
    ClientData data;

    public ClientAdapter(ClientContext context, ClientData data) {
        this.context = context;
        this.data = data;
    }
    void estadoSeguinte(ClientState newState) {
        context.estadoSeguinte(newState.createState(context, data));
    }
    @Override
    public boolean regista(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean login(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean edita(ArrayList<String> temp) {
        return false;
    }

    @Override
    public void avancar(int i) {
    }

    @Override
    public void regressar() {
    }

    @Override
    public boolean consulta(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean inserirEspetaculo(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean visivel(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean submeteReserva(ArrayList<String> temp) { return false; }

    @Override
    public boolean consultaReservasPagas(ArrayList<String> temp) {
        return false;
    }
    @Override
    public boolean consultaReservasParaPagamento(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean esperaPagamento(AtomicInteger pagamento) {
        return false;
    }
}