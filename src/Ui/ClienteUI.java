package Ui;

import Model.data.info;
import Model.fsm.ClientContext;
import utils.PDInput;

import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class ClienteUI{
    Scanner sc;
    ClientContext fsm;

    AtomicBoolean finish = new AtomicBoolean(false);
    AtomicBoolean confirmaUpdate = new AtomicBoolean(false);
    public ClienteUI(ClientContext fsm) {
        this.fsm = fsm;
        sc = new Scanner(System.in);

    }

    public void start() throws InterruptedException {

        fsm.conectaUDP();
        fsm.conectaTCP(confirmaUpdate);

        while (!finish.get()) {

            System.out.println("finish" + finish.get());
            if (confirmaUpdate.get()) {
                fsm.avancar();
                confirmaUpdate.getAndSet(false);
                System.out.println("AVANCEI");
            }
            switch (fsm.getState()) {
                case AUTENTICA -> autenticaUI();
                case LOGADO -> logadoUI();
                case ESPETACULO_ADMIN -> espetaculoUI();
                default -> finish.getAndSet(true);

            }
            sleep(300);
        }

        exit(0);
    }

    private void logadoUI() {
        switch (PDInput.chooseOption("*** Logado State ***", "Editar", "Consultar","Logado")) {
            case 1 -> { editaUI(); }
            case 2 -> { loginUI(); }
            case 3 -> {fsm.avancar();}
            default -> finish.getAndSet(true);
        }
    }

    private void espetaculoUI() {
        switch (PDInput.chooseOption("*** Espetaculo State Admin ***", "Inserir")) {
            case 1 -> {
                String filename = PDInput.readString("Filename:",false);
                ArrayList <String> temp = new ArrayList<>();
                Collections.addAll(temp, String.valueOf(info.INSERE_ESPETACULOS), filename);
                fsm.inserirEspetaculos(temp);
            }
            case 2 -> {loginUI();}
            default -> finish.getAndSet(true);
        }
    }

    private void autenticaUI() {
        switch (PDInput.chooseOption("*** Autentica State ***", "Registo", "Login", "Quit")) {
            case 1 -> { registoUI(); }
            case 2 -> { loginUI(); }
            case 3 -> { finish.getAndSet(true); }
        }
    }

    private boolean registoUI() {
        String name = PDInput.readString("Name: ",false );
        String username = PDInput.readString("Username: ", false);
        String password = PDInput.readString("Password: ", false);
        ArrayList <String> temp = new ArrayList<>();
        Collections.addAll(temp, String.valueOf(info.REGISTA_USER),name,username,password);
        if(fsm.regista(temp))
            return true;
        return false;
    }

    private boolean loginUI() {
        String username = PDInput.readString("Username: ", false);
        String password = PDInput.readString("Password: ", false);
        ArrayList<String> temp = new ArrayList<>();
        Collections.addAll(temp, String.valueOf(info.LOGIN_USER), username, password);
        if (fsm.login(temp))
            return true;
        return false;
    }

    private boolean editaUI() {
        ArrayList<String> temp = new ArrayList<>();
        switch (PDInput.chooseOption("*** Editar dados ***", "Name", "Username", "Password")) {
            case 1 -> {
                String name = PDInput.readString("New name: ", false);
                Collections.addAll(temp, String.valueOf(info.EDITA_NAME), name);
            }
            case 2 -> {
                String username = PDInput.readString("New username: ", false);
                Collections.addAll(temp, String.valueOf(info.EDITA_USERNAME), username);
            }
            case 3 -> {
                String password = PDInput.readString("New password: ", false);
                Collections.addAll(temp, String.valueOf(info.EDITA_PASSWORD), password); }
        }

        if(fsm.edita(temp))
            return true;
        return false;
    }

}
