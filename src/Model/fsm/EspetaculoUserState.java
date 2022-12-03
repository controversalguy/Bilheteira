package Model.fsm;

import Model.data.ClientData;

public class EspetaculoUserState extends ClientAdapter {
    public EspetaculoUserState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public ClientState getState() {
        return ClientState.ESPETACULO_USER;
    }
}
