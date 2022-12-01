package Ui;

import Model.data.info;
import Model.fsm.ClientContext;
import utils.PDInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClienteUI{
    Scanner sc;
    ClientContext fsm;

    AtomicBoolean finish = new AtomicBoolean(false);
    public ClienteUI(ClientContext fsm) {
        this.fsm = fsm;
        sc = new Scanner(System.in);

    }
    public void start() {

            fsm.conectaUDP();
            fsm.conectaTCP();

            while (!finish.get()) {
                switch (fsm.getState()) {
                    case AUTENTICA -> autenticaUI();
                    //case ESPETACULO -> espetaculoUI();
                }
            }
    }

    private void autenticaUI() {
        switch (PDInput.chooseOption("*** Autentica State ***", "Registo", "Login", "Quit")) {
            case 1 -> {
                System.out.println("finish: " + finish);if(!finish.get())registoUI();}
            case 2 -> {if(!finish.get())loginUI();}
            default -> finish.getAndSet(true);
        }
    }

    private void registoUI() {
        String name = PDInput.readString("Name: ",false );
        String username = PDInput.readString("Username: ", false);
        String password = PDInput.readString("Password: ", false);
        ArrayList <String> temp = new ArrayList<>();
        Collections.addAll(temp, String.valueOf(info.REGISTA_USER),name,username,password);
        fsm.regista(temp);
    }

    private void loginUI() {
        String username = PDInput.readString("Username: ", false);
        String password = PDInput.readString("Password: ", false);
        ArrayList <String> temp = new ArrayList<>();
        Collections.addAll(temp, String.valueOf(info.LOGIN_USER),username,password);
        fsm.login(temp);
    }
}
