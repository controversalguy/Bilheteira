package Model.fsm;

import Model.data.ClientData;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientContext {
    private IClientState state;
    ClientData data;

    public ClientContext(String strIpServer, int portServer) {
        data = new ClientData(strIpServer,portServer);
        state = ClientState.AUTENTICA.createState(this, data);
    }
    public void estadoSeguinte(IClientState newState) {
        state = newState;
    }
    public ClientState getState() {
        if (state == null) return null;
        return state.getState();
    }
    public void avancar() {
        state.avancar();
    }
    public void conectaTCP(AtomicBoolean confirma){
        data.connectaTCPServidor(confirma);
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

    public boolean edita(ArrayList<String> temp) { return state.edita(temp);}

    public boolean inserirEspetaculos(ArrayList<String> temp) {
        return state.inserirEspetaculo(temp);
    }
}
