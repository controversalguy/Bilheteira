package Ui;

import Model.data.info;
import Model.fsm.ClientContext;
import utils.PDInput;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;
import static java.lang.Thread.sleep;

public class ClienteUI{
    Scanner sc;
    ClientContext fsm;

    boolean finish = false;
    AtomicInteger confirmaUpdate = new AtomicInteger(0);
    AtomicInteger pagamento = new AtomicInteger(0);
    //AtomicBoolean confirmaUpdate = new AtomicBoolean(false);
    public ClienteUI(ClientContext fsm) {
        this.fsm = fsm;
        sc = new Scanner(System.in);

    }

    public void start() throws InterruptedException {

        fsm.conectaUDP();
        fsm.conectaTCP(confirmaUpdate);

        while (!finish) {

            if (confirmaUpdate.get() == 1) {
                fsm.avancar(1);
                confirmaUpdate.getAndSet(0);
            } else if(confirmaUpdate.get() == 2) {
                fsm.avancar(2);
                confirmaUpdate.getAndSet(0);
            } if(confirmaUpdate.get() == 4) {
                fsm.avancar(0);
                confirmaUpdate.getAndSet(0);
            }

            switch (fsm.getState()) {
                case AUTENTICA -> autenticaUI();
                case LOGADO_ADMIN -> logadoAdminUI();
                case LOGADO_USER -> logadoUserUI();
                case PAGAMENTO -> pagamentoUI();
                default -> finish = true;

            }
            sleep(300);
        }

        exit(0);
    }

    private void pagamentoUI() {
        System.out.println("*** Pagamento State ***, Efetuar Pagamento [1], Consultar Reservas Pagas [2], Consultar Reservas Pendentes [3]");
        fsm.esperaPagamento(pagamento);

        do {
            if(pagamento.get() == 1) {
                efetuaPagamento();
                pagamento.getAndSet(0);
            } else if(pagamento.get() == 2) {
                consultaReservasPagas();
                pagamento.getAndSet(0);
            } else if(pagamento.get() == 3) {
                consultaReservasPendentes();
                pagamento.getAndSet(0);
            }
        } while (pagamento.get() != 4);

        System.out.println("ACABOUUUUUU");

       // PDInput.chooseOption("*** Pagamento State ***", "Efetuar Pagamento", "Consultar Reservas Pagas", "Consultar Reservas Pendentes"))
//            case 1 ->if(!espera)
//            case 2 ->if(!espera) ;
//            case 3 ->if(!espera) ;
//            default -> finish = true;
    }

    private void efetuaPagamento() {
        System.out.println("FAZ PAGAMENTO");
    }

    private void consultaReservasPagas() {
        System.out.println("CONSULTA O QUE PAGAS");
    }

    private void consultaReservasPendentes() {
        System.out.println("DEVES AO PESSOAL BORRO");
    }

    private void logadoUserUI() throws InterruptedException {
        switch (PDInput.chooseOption("*** User State ***", "Editar Perfil", "Consultar Espetaculos", "Selecionar espetaculo")) {
            case 1 -> editaUI();
            case 2 -> consultarEspetaculos();
            case 3 -> selecionarEspetaculo();
            default -> finish = true;
        }
    }

    private void selecionarEspetaculo() throws InterruptedException {
        int numero = PDInput.readInt("Número do espetáculo: ");
        ArrayList <String> temp = new ArrayList<>();
        Collections.addAll(temp, String.valueOf(info.SELECIONAR_ESPETACULO), String.valueOf(numero));
        fsm.selecionarEspetaculo(temp);

        //TODO n deixar avancar se n existir confirmaUpdate

        sleep(300);

        if(confirmaUpdate.get() == 3) {
            confirmaUpdate.getAndSet(0);
            return;
        }

        System.out.println("\n");

        ArrayList<String> lugaresFila = new ArrayList<>();
        lugaresFila.add(String.valueOf(info.SUBMETE_RESERVA));
        lugaresFila.add(String.valueOf(numero));

        int continuar;

        do {
            String fila;
            do {

                fila = PDInput.readString("Fila: ", false);

            }while (fila.length() > 1 || PDInput.isNumeric(fila));

            int lugar = PDInput.readInt("Lugar: ");

            String par = fila + "-" + lugar;
            lugaresFila.add(par);


            continuar = PDInput.readInt("Deseja inserir mais lugares? [0 - Sim | Outro - Não]");
        } while (continuar == 0);

        System.out.println("Filas: " +  lugaresFila);

        fsm.submeteReserva(lugaresFila);

    }

    private void logadoAdminUI() {
        switch (PDInput.chooseOption("*** Admin State ***",  "Espetaculos")) {
            case 1-> { espetaculoUI(); }
            default -> finish = true;
        }
    }

    private void espetaculoUI() {
        switch (PDInput.chooseOption("*** Espetaculos  ***", "Inserir","Tornar visível","Consultar")) {
            case 1 -> {
                String filename = PDInput.readString("Filename:",false);
                ArrayList <String> temp = new ArrayList<>();
                Collections.addAll(temp, String.valueOf(info.INSERE_ESPETACULOS), filename);
                fsm.inserirEspetaculos(temp);
            }
            case 2 -> {
                int id = PDInput.readInt("Introduza o id do espetáculo: ");
                ArrayList <String> temp = new ArrayList<>();
                Collections.addAll(temp, String.valueOf(info.TORNA_VISIVEL),String.valueOf(id));
                fsm.visivel(temp);
            }
            case 3 -> consultarEspetaculos();
            default -> finish=true;
        }
    }

    private void consultarEspetaculos() {

        ArrayList<String> temp = new ArrayList<>();
        switch (PDInput.chooseOption("*** Consultar Espetaculos ***", "Descrição", "Tipo", "Data e hora", "Duração", "Local", "Localidade", "País", "Classificação Etária", "Todos")) {
            case 1 -> {
                String filtro = PDInput.readString("Insira a Descrição: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "0", filtro);
                fsm.consulta(temp);
            }
            case 2 -> {
                String filtro = PDInput.readString("Insira o Tipo: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "1", filtro);
                fsm.consulta(temp);
            }
            case 3 -> {
                String filtro = PDInput.readString("Insira a Data e hora: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "2", filtro);
                fsm.consulta(temp);
            }
            case 4 -> {
                String filtro = PDInput.readString("Insira a Duração: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "3", filtro);
                fsm.consulta(temp);
            }
            case 5 -> {
                String filtro = PDInput.readString("Insira o Local: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "4", filtro);
                fsm.consulta(temp);
            }
            case 6 -> {
                String filtro = PDInput.readString("Insira a Localidade: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "5", filtro);
                fsm.consulta(temp);
            }
            case 7 -> {
                String filtro = PDInput.readString("Insira o País: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "6", filtro);
                fsm.consulta(temp);
            }
            case 8 -> {
                String filtro = PDInput.readString("Insira a Classificação Etária: ", false);
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "7", filtro);
                fsm.consulta(temp);
            }
            case 9 -> {
                Collections.addAll(temp, String.valueOf(info.FILTRO_ESPETACULO), "8", "Todos");
                fsm.consulta(temp);
            }
        }
    }

    private void autenticaUI() {
        switch (PDInput.chooseOption("*** Autentica State ***", "Registo", "Login", "Quit")) {
            case 1 -> { registoUI(); }
            case 2 -> { loginUI(); }
            case 3 -> {finish=true; }
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
