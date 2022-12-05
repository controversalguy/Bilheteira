package Model.Servidor;

import utils.Msg;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static Model.Servidor.Servidor.connDB;
import static Model.Servidor.Servidor.portServer;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;
    ArrayList<Informacoes> listaServidores;
    static AtomicBoolean threadCorre;
    ArrayList<ObjectOutputStream> listOos;
    public ListenHeartBeat(MulticastSocket ms, ArrayList<Informacoes> listaServidores,AtomicBoolean threadCorre,ArrayList<ObjectOutputStream> listOos) {
        this.ms = ms;
        this.listaServidores = listaServidores;
        this.threadCorre = threadCorre;
        this.listOos = listOos;
    }

    @Override
    public void run() {
        while(threadCorre.get()) {
            DatagramPacket dp = new DatagramPacket(new byte[4000], 4000);
            System.out.println("A ESPERA DE RECEBER");
            try {
                ms.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Msg msg;
            Informacoes info;

            try {
                info = (Informacoes) ois.readObject();
                synchronized (listaServidores) {
                    //Informacoes info = new Informacoes(msg.getPortoServer(),msg.getIp(),msg.getLigacoesTCP());
                    if (!listaServidores.contains(info)){
                        /*&& info.isDisponivel()*/
                        listaServidores.add(info);
                        Comparator<Informacoes> compare = new InformacoesComparator();
                        listaServidores.sort(compare);

                        for (ObjectOutputStream os: listOos) {
                            enviaListaServidoresAtualizada(os);
                        }

                    }else { // se já existir

                        listaServidores.set(listaServidores.lastIndexOf(info), info);

                        Comparator<Informacoes> compare = new InformacoesComparator();
                        listaServidores.sort(compare);
                    }


                }

                if (info.getMsgAtualiza() != null) {

                    ArrayList<String> msgSocket;

                    System.err.println("LISTEN BABY " + info.getMsgAtualiza());
                    if (info.getMsgAtualiza().equalsIgnoreCase("PREPARE") && info.getPorto() != portServer) {
                        System.out.println("ListenHeartBeatAtualiza" + info);

                        msgSocket = info.getMsgSockett();

                        enviaUDP(info.getPortoUDPAtualiza(), info.getVersaoBdAtualiza(), info.getIp());
                            LocalDateTime entraWhile = LocalDateTime.now();
                        while (true) {
                            LocalDateTime atual = LocalDateTime.now();
                            long seconds = ChronoUnit.SECONDS.between(entraWhile,atual);
                            System.out.println("seconds: "+ seconds);
                            if(seconds > 2){
                                System.out.println("Esperei 3 segundos");
                                break;
                            }
                            System.err.println("RECEBIIIIIIII");
                            ms.receive(dp);
                            bais = new ByteArrayInputStream(dp.getData());
                            try {
                                ois = new ObjectInputStream(bais);
                                info = (Informacoes) ois.readObject();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (info.getMsgAtualiza() != null) {
                                System.out.println("MSG ATUALIZA LISTEN: " + info.getMsgAtualiza());
                                if (info.getMsgAtualiza().equalsIgnoreCase("Commit")) {
                                    System.out.println("Recebi Commit QUERO ATUALIZAR");
                                    if(msgSocket!=null){
                                        processaAtualizacao(msgSocket);
                                    }
                                    break;
                                } else if (info.getMsgAtualiza().equalsIgnoreCase("Abort")) {
                                    System.out.println("Recebi Abort");
                                    break;
                                }else {
                                    System.out.println("IGNORADO");
                                }
                            }
                        }
                        System.out.println("RECEBA MM HEIN LISTEN");
                    }
                }
            } catch (IOException | ClassNotFoundException | SQLException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("[INFO] ListenHeartBeat terminado com sucesso!");
    }

    private void processaAtualizacao(ArrayList<String> msgSocket) throws SQLException {
        switch (msgSocket.get(0)) {
            case "REGISTA_USER" -> {
                switch (connDB.insertUser(msgSocket,false)) {
                    case ADMIN_NAO_PODE_REGISTAR -> {
                        //msg.setMsg("\nImpossível registar como admin");
                    }
                    case CLIENTE_REGISTADO_SUCESSO -> {
                       // msg.setMsg("\nCliente registado com sucesso!");

                        //commit
                    }

                    case CLIENTE_JA_REGISTADO -> {
                       // msg.setMsg("\nCliente já registado!");
                    }
                }
            }
            case "LOGIN_USER" -> {
                String str = connDB.logaUser(msgSocket,false);
               // msg.setMsg("\n" + str);
            }
            case "EDITA_NAME" -> {
                String str = connDB.updateUser(msgSocket, 0,false);
                //msg.setMsg("\n" + str);
            }
            case "EDITA_USERNAME" -> {
                String str = connDB.updateUser(msgSocket, 1,false);
               // msg.setMsg("\n" + str);
            }
            case "EDITA_PASSWORD" -> {
                String str = connDB.updateUser(msgSocket, 2,false);
               // msg.setMsg("\n" + str);
            }
            case "INSERE_ESPETACULOS" -> {

                String str = connDB.insereEspetaculos(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "TORNA_VISIVEL" -> {
                String str = connDB.tornaVisivel(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "FILTRO_ESPETACULO"->{
                String str = connDB.filtraEspetaculo(Integer.parseInt(msgSocket.get(1)),msgSocket.get(2), msgSocket.get(3),false);
                //msg.setMsg("\n" + str);
            }
            case "SELECIONAR_ESPETACULO"->{
                String str = connDB.selecionaEspetaculo(Integer.parseInt(msgSocket.get(1)),false);
                //msg.setMsg("\n" + str);
            }
            case "SUBMETE_RESERVA"->{
                String str = connDB.submeteReserva(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "EFETUA_PAGAMENTO"->{
                String str = connDB.efetuaPagamento(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "LIMITE_TEMPO"->{
                String str = connDB.retiraReservaLimiteTempo(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "CONSULTA_RESERVAS_PAGAS"->{
                String str = connDB.consultaReservasPagas(msgSocket.get(1));
                //msg.setMsg("\n" + str);
            }
            case "CONSULTA_RESERVAS_PENDENTES"->{
                String str = connDB.consultaReservasPendentes(msgSocket.get(1));
               // msg.setMsg("\n" + str);
            }
            case "ELIMINA_ESPETACULO"->{
                String str = connDB.eliminarEspetaculo(msgSocket,false);
                //msg.setMsg("\n" + str);
            }
            case "LOGOUT"->{
                System.out.println("LOGOUTTTTTTT");
                String str = connDB.logout(msgSocket,false);
                //msg.setMsg("\n" + str);
            }

        }
    }

    private void enviaUDP(int portoUDPAtualiza, int versaoBdAtualizada, String ip) throws IOException {
        DatagramSocket ds = new DatagramSocket();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        Msg msg = new Msg();
        msg.setVersaoBdAtualizada(versaoBdAtualizada);

        System.out.println("EU enviaUDP");

        oos.writeUnshared(msg);
        byte[] messageBytes = baos.toByteArray();

        InetAddress ipServer = InetAddress.getByName(ip);

        DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portoUDPAtualiza);
        ds.send(dp);
    }

    void enviaListaServidoresAtualizada(ObjectOutputStream oos) {
        Msg msg = new Msg();
        try {
            Iterator<Informacoes> iterator = listaServidores.iterator();

            while (iterator.hasNext()) {
                Informacoes info = iterator.next();

                msg.setPortoServer(info.getPorto());
                msg.setIp(info.getIp());
                msg.setLigacoesTCP(info.getLigacoes());
                msg.setIndex(listaServidores.indexOf(info));
                System.out.println("MSGATUALIZA: " + msg);
                if (!iterator.hasNext()) {
                    msg.setLastPacket(true);
                }
                oos.writeUnshared(msg);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



