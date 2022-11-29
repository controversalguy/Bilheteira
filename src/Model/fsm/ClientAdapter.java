package Model.fsm;

import Model.Servidor.Cliente;
import Model.data.ClientData;

abstract class ClientAdapter implements IClientState {
    ClientContext context;
    ClientData data;

    public ClientAdapter(ClientContext context, ClientData data) {
        this.context = context;
        this.data = data;
    }

    @Override
    public boolean regista(String email, String username, String password) {
        return false;
    }
}