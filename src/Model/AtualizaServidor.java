package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtualizaServidor extends Thread {

    ArrayList<Informacoes> listaServidores;
    ConnDB connDB;
    AtomicBoolean disponivel;
    MulticastSocket ms;
    InetAddress ipgroup;
    int portTCP;
    String ipServer;
    ArrayList<ObjectOutputStream> listOos;
    public AtualizaServidor(ArrayList<Informacoes> listaServidores, MulticastSocket ms, InetAddress ipgroup, int portTCP, String ipServer,
                            ConnDB connDB, AtomicBoolean disponivel, ArrayList<ObjectOutputStream> listOos) {
        this.listaServidores = listaServidores;
        this.connDB = connDB;
        this.disponivel = disponivel;
        this.ms = ms;
        this.ipgroup = ipgroup;
        this.portTCP = portTCP;
        this.ipServer = ipServer;
        this.listOos = listOos;
    }

    @Override
    public void run() {
        while (true) {
            synchronized (listaServidores) {
                int valMaior = connDB.getVersao().get();
                int posMaior = -1;
                for (int i = 0; i < listaServidores.size(); i++) {
                    if (listaServidores.get(i).getVersaoBd() > valMaior) {
                        valMaior = listaServidores.get(i).getVersaoBd();
                        posMaior = i;// posicao do Servidor que tem maior versao
                    }
                }

                if (posMaior > -1) { // é o maior
                    System.out.println("ENTREI -1");
                    disponivel.getAndSet(false);
                    Servidor.atualiza(ms, ipgroup, portTCP, ipServer, connDB);
                    for (ObjectOutputStream os: listOos) {enviaListaServidoresAtualizada(os);}
                }
            }
            disponivel.getAndSet(true);
        }
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
