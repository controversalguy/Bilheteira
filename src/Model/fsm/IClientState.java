package Model.fsm;

public interface IClientState {
    ClientState getState();

    boolean regista(String email, String username, String password);
}
