package Model.fsm;

import Model.data.ClientData;

public class EspetaculoState extends ClientAdapter {
    public EspetaculoState(ClientContext context, ClientData data) {
        super(context,data);
    }

    @Override
    public ClientState getState() {
        return ClientState.ESPETACULO;
    }
}
