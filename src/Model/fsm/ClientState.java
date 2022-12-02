package Model.fsm;

import Model.data.ClientData;

public enum ClientState {
    AUTENTICA,LOGADO,ESPETACULO;

    IClientState createState(ClientContext context, ClientData data) {
        return switch (this) {
            case AUTENTICA -> new AutenticaState(context, data);
            case LOGADO -> new LogadoState(context,data);
            case ESPETACULO -> new EspetaculoState(context,data);
        };
    }
}
