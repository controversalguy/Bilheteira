package Model.Servidor;

import Model.data.ClientData;
import utils.Msg;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class ClientReceiveTCP extends Thread {
    ArrayList<Informacoes> listaServidores;
    Socket sClient;
    ClientData data;

    public ClientReceiveTCP(ArrayList<Informacoes> listaServidores, Socket sClient, ClientData data) {
        this.listaServidores = listaServidores;
        this.sClient = sClient;
        this.data = data;
    }

    @Override
    public void run() {
        Msg msgTCP;
        try {
            InputStream is = sClient.getInputStream();
            ObjectInputStream oisTCP = new ObjectInputStream(is);

            while (true) {

                Object msg = oisTCP.readObject();
                if (msg instanceof Msg) {
                    msgTCP = (Msg) msg;
                    if(msgTCP.getMsg()!=null){
                        System.out.println(msgTCP.getMsg());
                    }else{
                        Informacoes info1 = new Informacoes(msgTCP.getPortoServer(), msgTCP.getIp(), msgTCP.getLigacoesTCP());
                        if (!listaServidores.contains(info1)) //se nao tiver na lista
                            listaServidores.add(info1);
                        else //se ja tiver mas for disponivel
                            listaServidores.set(listaServidores.lastIndexOf(info1), info1);

                        System.out.println("ListaServersCliente: " + listaServidores);

                    }

                }

            }
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SocketException e) {
            data.connectaTCPServidor();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

