package Model.fsm;

import Model.data.ClientData;

public class ClientContext {
    private IClientState state;
    ClientData data;

    public ClientContext(String strIpServer, int portServer) {
        data = new ClientData(strIpServer,portServer);
        state = ClientState.AUTENTICA.createState(this, data);
    }

    public ClientState getState() {
        if (state == null) return null;
        return state.getState();
    }

    public boolean conectaTCP(){
        return data.connectaTCPServidor();
    }

    public boolean regista(String email, String username, String password) {
        return state.regista(email,username,password);
    }

    public boolean conectaUDP() {
        return  data.connectaServidorUDP();
    }
}
