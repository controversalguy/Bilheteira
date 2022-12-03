package Model.fsm;

import Model.data.ClientData;

public enum ClientState {
    AUTENTICA,LOGADO, ESPETACULO_ADMIN, ESPETACULO_USER;

    IClientState createState(ClientContext context, ClientData data) {
        return switch (this) {
            case AUTENTICA -> new AutenticaState(context, data);
            case LOGADO -> new LogadoState(context,data);
            case ESPETACULO_ADMIN -> new EspetaculoAdminState(context,data);
            case ESPETACULO_USER -> new EspetaculoUserState(context,data);
        };
    }
}
