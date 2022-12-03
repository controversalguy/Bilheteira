package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

public class LogadoState extends ClientAdapter {
    public LogadoState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean edita(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public void avancar() {
        estadoSeguinte(ClientState.ESPETACULO_ADMIN);
    }

    @Override
    public ClientState getState() {
        return ClientState.LOGADO;
    }
}
