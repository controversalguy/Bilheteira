package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

public class EspetaculoUserState extends ClientAdapter {
    public EspetaculoUserState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean consulta(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public ClientState getState() {
        return ClientState.ESPETACULO_USER;
    }
}
