package Model;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ComunicaTCP extends Thread {
    MulticastSocket ms;
    Socket socketCli;
    AtomicInteger ligacoesTCP;

    public ComunicaTCP(MulticastSocket ms, Socket socketCli,AtomicInteger ligacoesTCP) {
        this.ms = ms;
        this.socketCli = socketCli;
        this.ligacoesTCP = ligacoesTCP;
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
                ObjectOutputStream oosSocket = new ObjectOutputStream(os);
                while (true){
                    Msg msgSocket = (Msg) oisSocket.readObject();
                    System.out.println("E"+msgSocket.getMsg());
                    oosSocket.writeUnshared(msgSocket);
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
