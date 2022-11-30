package Model.Servidor;

import utils.Msg;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ComunicaTCP extends Thread {
    Socket socketCli;
    AtomicInteger ligacoesTCP;
    String dbName;
    ArrayList<ObjectOutputStream> listaOos;
    AtomicBoolean disponivel;
    AtomicBoolean threadCorre;

    public ComunicaTCP( Socket socketCli, AtomicInteger ligacoesTCP, String dbName, AtomicBoolean disponivel,
                        ArrayList<ObjectOutputStream> listaOos, AtomicBoolean threadCorre) {
        this.socketCli = socketCli;
        this.ligacoesTCP = ligacoesTCP;
        this.dbName = dbName;
        this.disponivel = disponivel;
        this.listaOos = listaOos;
        this.threadCorre = threadCorre;
    }

    @Override
    public void run() {
        try {
            ligacoesTCP.getAndIncrement();
            InputStream is = socketCli.getInputStream();
            OutputStream os = socketCli.getOutputStream();
            ObjectInputStream oisSocket = new ObjectInputStream(is);
            ObjectOutputStream oos = new ObjectOutputStream(os);

            synchronized (listaOos) {
                if (!listaOos.contains(oos)) {
                    listaOos.add(oos);
                    Servidor.atualiza("listaOos", -3);
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
                } else {
                    System.out.println("MSGSOCKET: " + msgSocket);
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
        }
        System.out.println("[INFO] ComunicaTCP terminado com sucesso!");
    }
}
