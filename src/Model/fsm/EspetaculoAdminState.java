package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

public class EspetaculoAdminState extends ClientAdapter {
    public EspetaculoAdminState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public boolean inserirEspetaculo(ArrayList<String> temp) {
        return data.enviaInfo(temp);
    }

    @Override
    public ClientState getState() {
        return ClientState.ESPETACULO_ADMIN;
    }
}
