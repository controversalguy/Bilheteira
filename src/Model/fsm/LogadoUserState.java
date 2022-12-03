package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

public class LogadoUserState extends ClientAdapter {
    public LogadoUserState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean edita(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public boolean consulta(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public boolean selecionar(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public void avancar(int i) {
        // TODO estadoSeguinte(ClientState.);
    }

    @Override
    public ClientState getState() {
        return ClientState.LOGADO_USER;
    }
}
