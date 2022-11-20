package Model;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ComunicaTCP extends Thread {
    MulticastSocket ms;
    ServerSocket ss;
    AtomicInteger ligacoesTCP;

    public ComunicaTCP(MulticastSocket ms, ServerSocket ss,AtomicInteger ligacoesTCP) {
        this.ms = ms;
        this.ss = ss;
        this.ligacoesTCP = ligacoesTCP;
    }

    @Override
    public void run() {
        Socket socketCli = null;
        try {
                //System.out.println("Fico a espera");
                socketCli = ss.accept();
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
