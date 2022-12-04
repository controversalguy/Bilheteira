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
    public boolean submeteReserva(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public void avancar(int i) {
       estadoSeguinte(ClientState.PAGAMENTO);
    }

    @Override
    public ClientState getState() {
        return ClientState.LOGADO_USER;
    }
}
