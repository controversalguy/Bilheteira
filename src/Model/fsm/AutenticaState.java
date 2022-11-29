package Model.fsm;

import Model.data.ClientData;

public class AutenticaState extends ClientAdapter {
    public AutenticaState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean regista(String email, String username, String password) {


        return false;
    }

    @Override
    public ClientState getState() {
        return ClientState.AUTENTICA;
    }
}
