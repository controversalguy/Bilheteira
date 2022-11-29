package Ui;

import Model.Servidor.Cliente;
import Model.fsm.ClientContext;
import utils.PDInput;

import java.util.Scanner;

public class ClienteUI {
    Cliente cliente;
    Scanner sc;
    ClientContext fsm;

    boolean finish = false;


    public ClienteUI(ClientContext fsm) {
        this.fsm = fsm;
        sc = new Scanner(System.in);
    }

    public void start(){
        fsm.conectaUDP();
        fsm.conectaTCP();
        while (!finish) {
            switch (fsm.getState()) {
                case AUTENTICA -> autenticaUI();
                //case ESPETACULO -> espetaculoUI();
            }
        }
    }

    private void autenticaUI() {
        switch (PDInput.chooseOption("*** Autentica State ***", "Registo", "Login", "Quit")) {
            case 1 -> registoUI();
            case 2 -> loginUI();
            default -> finish = true;
        }
    }

    private void registoUI() {
        String email = PDInput.readString("Email: ",false );
        String username = PDInput.readString("Username: ", false);
        String password = PDInput.readString("Password: ", false);

        fsm.regista(email, username, password);
    }

    private void loginUI() {

    }

}
