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

    @Override
    public boolean regista(ArrayList<String> temp) {
        return false;
    }

    @Override
    public boolean login(ArrayList<String> temp) {
        return false;
    }
}