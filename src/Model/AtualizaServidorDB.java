package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtualizaServidorDB extends Thread {

    ArrayList<Informacoes> listaServidores;
    ConnDB connDB;
    AtomicBoolean disponivel;
    ArrayList<ObjectOutputStream> listOos;
    AtomicBoolean threadCorre;

    public AtualizaServidorDB(ArrayList<Informacoes> listaServidores, ConnDB connDB, AtomicBoolean disponivel,
                              ArrayList<ObjectOutputStream> listOos, AtomicBoolean threadCorre) {
        this.listaServidores = listaServidores;
        this.connDB = connDB;
        this.disponivel = disponivel;
        this.listOos = listOos;
        this.threadCorre = threadCorre;
    }
    @Override
    public void run() {
        while (threadCorre.get()) {
            int valMaior = connDB.getVersao().get();
            int posMaior = -1;
            synchronized (listaServidores) {

                for (int i = 0; i < listaServidores.size(); i++) {
                    if (listaServidores.get(i).getVersaoBd() > valMaior) {
                        valMaior = listaServidores.get(i).getVersaoBd();
                        posMaior = i;// posicao do Servidor que tem maior versao
                    }
                }
            }
                if (posMaior > -1) {
                    System.out.println("ENTREI -1");
                    disponivel.getAndSet(false);
                    Servidor.atualiza("prepare",valMaior);
                    for (ObjectOutputStream os: listOos) {
                        enviaListaServidoresAtualizada(os);
                    }

                }

            disponivel.getAndSet(true);
        }
        System.out.println("[INFO] AtualizaServidor terminado com sucesso!");
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
