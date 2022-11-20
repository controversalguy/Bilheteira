package Model;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ComunicaTCP extends Thread {
    MulticastSocket ms;
    Socket socketCli;
    public ComunicaTCP(MulticastSocket ms, Socket socketCli) {
        this.ms = ms;
        this.socketCli = socketCli;
    }

    @Override
    public void run() {
            try {
                InputStream is = socketCli.getInputStream();
                OutputStream os = socketCli.getOutputStream();
                ObjectInputStream oisSocket = new ObjectInputStream(is);
                ObjectOutputStream oosSocket = new ObjectOutputStream(os);
                while (true){
                    Msg msgSocket = (Msg) oisSocket.readObject();
                    System.out.println(msgSocket.getMsg());
                    oosSocket.writeUnshared(msgSocket);
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Classe Não encontrada");
            } catch (IOException e) {
                System.out.println("Erro em stream");
            }
        }
}
