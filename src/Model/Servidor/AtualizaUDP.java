package Model.Servidor;

import utils.Msg;

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
        DatagramPacket dp = new DatagramPacket(new byte[255], 255);
        System.out.println("ENTREI AtualizaUDP");

        //Iterator<Informacoes> it = listaServidores.iterator();
        //System.out.println("ItAntes: " + it.hasNext());

        System.out.println("LISTA ANTES: " + listaServidores);
        //while (it.hasNext() && threadCorre.get()) { // TODO VERIIFCAR DB VERSAO

        System.out.println(listaServidores);
            for(int i = 0; i < listaServidores.size(); i++) {

            System.out.println("LISTA dentro: " + listaServidores);
           // it.next();
            //System.out.println("It: " + it.hasNext());
            try {
                ds.setSoTimeout(1000);
                ds.receive(dp);
                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                Msg msg = (Msg) ois.readObject();
                System.out.println("It: " + msg.getVersaoBdAtualizada());
                System.out.println("Versao [" + msg.getVersaoBdAtualizada() + "]");

            } catch (SocketTimeoutException e) {
                System.out.println("SocketTimeoutException");
                if (tentativas.get() < 1) {
                    Servidor.atualiza("Prepare", valMaior, null);
                    tentativas.getAndIncrement();
                    System.out.println("[INFO] AtualizaUDP terminado com sucesso! < 1)");
                    return;
                } else {
                    System.out.println("!QUE TAS AQUI A FAZER");
                    Servidor.atualiza("Abort", valMaior, null);
                    return;
                }
                // Se for a segunda tentativa manda Abort
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            //System.out.println("!GANDA TONE ATUALIZAUDP");
        }

        Servidor.atualiza("Commit", valMaior, null);

        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");

    }
}
