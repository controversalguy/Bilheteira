package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

import static Model.fsm.ClientState.LOGADO;

public class AutenticaState extends ClientAdapter {
    public AutenticaState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean regista(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public boolean login(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public void avancar() {
        estadoSeguinte(LOGADO);
    }

    @Override
    public ClientState getState() {
        return ClientState.AUTENTICA;
    }
}
