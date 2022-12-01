package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;

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

    public boolean regista(ArrayList<String> temp) {
        return state.regista(temp);
    }

    public boolean login(ArrayList<String> temp) {
        return state.login(temp);
    }

    public boolean conectaUDP() {
        return  data.connectaServidorUDP();
    }
}
