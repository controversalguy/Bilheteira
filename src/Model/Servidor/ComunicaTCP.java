package Model.Servidor;

import utils.Msg;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static Model.Servidor.Servidor.connDB;

public class ComunicaTCP extends Thread {
    Socket socketCli;
    AtomicInteger ligacoesTCP;
    String dbName;
    ArrayList<ObjectOutputStream> listaOos;
    AtomicBoolean disponivel;
    AtomicBoolean threadCorre;
    ArrayList<Informacoes> listaServidores;

    public ComunicaTCP(Socket socketCli, AtomicInteger ligacoesTCP, String dbName, AtomicBoolean disponivel,
                       ArrayList<ObjectOutputStream> listaOos, AtomicBoolean threadCorre, ArrayList<Informacoes> listaServidores) {
        this.socketCli = socketCli;
        this.ligacoesTCP = ligacoesTCP;
        this.dbName = dbName;
        this.disponivel = disponivel;
        this.listaOos = listaOos;
        this.threadCorre = threadCorre;
        this.listaServidores = listaServidores;
    }

    @Override
    public void run() {
        try {

            ligacoesTCP.getAndIncrement();

            System.out.println("MAIS UM CLIENTE PA CONTA");


            InputStream is = socketCli.getInputStream();
            OutputStream os = socketCli.getOutputStream();
            ObjectInputStream oisSocket = new ObjectInputStream(is);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            synchronized (listaOos) {
                if (!listaOos.contains(oos)) {
                    listaOos.add(oos);
                    Servidor.atualiza("listaOos", -3);

                    synchronized (listaServidores) {
                        Comparator<Informacoes> compare = new InformacoesComparator();
                        listaServidores.sort(compare);
                    }

                    for (ObjectOutputStream o : listaOos) {
                        System.out.println("ERROUUUUUUUUUUUUUUUUUUUUUUUUU");
                        enviaListaServidoresAtualizada(o);
                    }
                }
            }

            //System.out.println("COMUNICATCP:" + msgSockett.getMsg())

            while (threadCorre.get()) {
                Object msgSocket = oisSocket.readObject();
                System.out.println("MSGSOCKETCARALHOSMAFODA: " + msgSocket);
                if (msgSocket instanceof Msg) {
                    Msg msgSockett = (Msg) msgSocket;
                    if (msgSockett.getMsg() != null) {
                        if (msgSockett.getMsg().equals("CloneBD")) {

                            System.out.println("[INFO] A clonar DataBase...");
                            disponivel.getAndSet(false);
                            Servidor.atualiza("CloneMyDB", -2);
                            FileInputStream fis = new FileInputStream(dbName);
                            byte[] bufferClient = new byte[4000];
                            int nBytes;
                            do {
                                nBytes = fis.read(bufferClient);
                                Msg msg = new Msg();
                                msg.setMsgBuffer(bufferClient);
                                msg.setMsgSize(nBytes);
                                if (nBytes == -1) {
                                    msg.setMsgBuffer(new byte[4000]);
                                    msg.setMsgSize(0);
                                    msg.setLastPacket(true);
                                } else
                                    msg.setLastPacket(false);
                                oos.reset();
                                oos.writeUnshared(msg);

                            } while (nBytes != -1);
                        }
                    }
                } else if (msgSocket instanceof ArrayList<?>) {
                    ArrayList<?> msgSockett = (ArrayList<?>) msgSocket;
                    Msg msg = new Msg();
                    System.out.println(msgSockett.get(0));
                    switch ((String) msgSockett.get(0)) {
                        case "REGISTA_USER" -> {
                            switch (connDB.insertUser((String) msgSockett.get(1), (String) msgSockett.get(2), (String) msgSockett.get(3))) {
                                case ADMIN_NAO_PODE_REGISTAR -> {
                                    msg.setMsg("\nImpossível registar como admin");
                                }
                                case CLIENTE_REGISTADO_SUCESSO -> {
                                    msg.setMsg("\nCliente registado com sucesso!");
                                    //commit
                                }

                                case CLIENTE_JA_REGISTADO -> {
                                    msg.setMsg("\nCliente já registado!");
                                }
                            }
                        }
                        case "LOGIN_USER" -> {
                            String str = connDB.logaUser((String) msgSockett.get(1), (String) msgSockett.get(2));
                            msg.setMsg("\n" + str);
                        }

                    }
                    oos.writeUnshared(msg);

                }
            }
        } catch (ClassNotFoundException e) {
            System.out.println("Classe Não encontrada");
        } catch (IOException e) {
            try {
                socketCli.close();
                ligacoesTCP.getAndDecrement();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("[INFO] ComunicaTCP terminado com sucesso!");
    }
    void enviaListaServidoresAtualizada(ObjectOutputStream oos) {
        Msg msg = new Msg();
        try {
            synchronized (listaServidores){

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

            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

