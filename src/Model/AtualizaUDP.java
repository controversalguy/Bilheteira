package Model;

import ConnectDatabase.ConnDB;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class AtualizaUDP extends Thread{
    DatagramSocket ds;
    ConnDB connDB;
    ArrayList<Informacoes> listaServidores;
    AtomicBoolean threadCorre;
    AtomicInteger tentativas;
    int valMaior;

    public AtualizaUDP(DatagramSocket ds, ConnDB connDB, ArrayList<Informacoes> listaServidores, AtomicBoolean threadCorre, AtomicInteger tentativas, int valMaior) {
        this.ds = ds;
        this.connDB = connDB;
        this.listaServidores = listaServidores;
        this.threadCorre = threadCorre;
        this.tentativas = tentativas;
        this.valMaior = valMaior;
    }

    @Override
    public void run() {
        DatagramPacket dp = new DatagramPacket(new byte[255],255);
        synchronized (listaServidores){
            Iterator<Informacoes> it = listaServidores.iterator();
            while(it.hasNext() && threadCorre.get()){
                it.next();
                try {
                    ds.setSoTimeout(1000);
                    ds.receive(dp);
                    ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);

                    Msg msg = (Msg) ois.readObject();
                    System.out.println("Versao [" + msg.getVersaoBdAtualizada()+"]");
                } catch (SocketTimeoutException e) {
                    if(tentativas.get() < 1){
                        Servidor.atualiza("Prepare", valMaior);
                        tentativas.getAndIncrement();
                        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");
                        return;
                    }
                    // Se for a segunda tentativa manda Abort
                        Servidor.atualiza("Abort", valMaior);
                }catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Servidor.atualiza("Commit", valMaior);
            System.out.println("Recebi de todos");

        }

        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");

    }
}
