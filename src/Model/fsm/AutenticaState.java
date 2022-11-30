package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

public class AutenticaState extends ClientAdapter {
    public AutenticaState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean regista(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public ClientState getState() {
        return ClientState.AUTENTICA;
    }
}
