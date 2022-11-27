package Model;

import ConnectDatabase.ConnDB;
import com.sun.jdi.ArrayReference;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class ComunicaTCP extends Thread {
    MulticastSocket ms;
    Socket socketCli;
    AtomicInteger ligacoesTCP;
    String dbName;

    ArrayList<ObjectOutputStream> listaOos;
    AtomicBoolean disponivel;
    InetAddress ipgroup;
    int portTCP;
    String ipServer;
    ConnDB connDB;
    public ComunicaTCP(MulticastSocket ms, Socket socketCli, AtomicInteger ligacoesTCP, String dbName, AtomicBoolean disponivel, ArrayList<ObjectOutputStream> listaOos,
                       InetAddress ipgroup, int portTCP, String ipServer, ConnDB connDB) {
        this.ms = ms;
        this.socketCli = socketCli;
        this.ligacoesTCP = ligacoesTCP;
        this.dbName = dbName;
        this.disponivel = disponivel;
        this.listaOos = listaOos;
        this.ipgroup = ipgroup;
        this.portTCP = portTCP;
        this.ipServer = ipServer;
        this.connDB = connDB;
    }

    @Override
    public void run() {
        try {
                //System.out.println("Fico a espera");
                //socketCli = ss.accept();
                //System.out.println("BOTA LUMEEEEEEEEEEE");
                ligacoesTCP.getAndIncrement();
                InputStream is = socketCli.getInputStream();
                OutputStream os = socketCli.getOutputStream();
                ObjectInputStream oisSocket = new ObjectInputStream(is);
                ObjectOutputStream oos = new ObjectOutputStream(os);
                while (true){
                    Msg msgSocket = (Msg) oisSocket.readObject();

                    if(msgSocket.getMsg().equals("CloneBD")) {

                        System.out.println("[INFO] A clonar DataBase...");
                        disponivel.getAndSet(false);
                        FileInputStream fis = new FileInputStream(dbName);
                        byte[] bufferClient = new byte[4000];
                        int nBytes;
                        do{
                            nBytes = fis.read(bufferClient);
                            Msg msg = new Msg();
                            msg.setMsgBuffer(bufferClient);
                            msg.setMsgSize(nBytes);
                            if(nBytes == -1){
                                msg.setMsgBuffer(new byte[4000]);
                                msg.setMsgSize(0);
                                msg.setLastPacket(true);
                            }else
                                msg.setLastPacket(false);
                            oos.reset();
                            oos.writeUnshared(msg);
                            //System.out.println("NBytes Lidos" + nBytes);
                            //out.write(bufferClient);
                        }while(nBytes != -1);

                    } else {
                        synchronized (listaOos) {
                            if (!listaOos.contains(oos)) {
                                System.out.println("SE O DIZES");
                                listaOos.add(oos);
                                Servidor.atualiza(ms, ipgroup, portTCP, ipServer, connDB);
                            }
                        }
                        System.out.println("E" + msgSocket.getMsg());
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
        }


}
