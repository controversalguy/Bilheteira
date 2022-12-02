package Model.fsm;

import Model.Servidor.Cliente;
import Model.data.ClientData;

import java.util.ArrayList;

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
    public void avancar() {
    }

    @Override
    public void regressar() {
    }

}