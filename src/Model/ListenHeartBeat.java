package Model;

import javax.imageio.metadata.IIOMetadataNode;
import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;
    ArrayList<Informacoes> listaServidores;
    //HashMap<Integer,String> listaServidores;

    public ListenHeartBeat(MulticastSocket ms, ArrayList<Informacoes> listaServidores) {
        this.ms = ms;
        this.listaServidores = listaServidores;
    }

    @Override
    public void run() {
        while(true) {
            DatagramPacket dp = new DatagramPacket(new byte[256], 256);

            try {
                ms.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            Msg msg;
            try {
                msg = (Msg) ois.readObject();
                synchronized (listaServidores) {
                    Informacoes info = new Informacoes(msg.getPortoServer(),msg.getIp(),msg.getLigacoesTCP());
                    if(!listaServidores.contains(info))
                        listaServidores.add(info);
                    else { // se já existir
                        //System.out.println("EXISTE BABY");
                        listaServidores.set(listaServidores.lastIndexOf(info),info);
                        //System.out.println("Info existente: "+ info);
                    }
                    Comparator<Informacoes> compare = new InformacoesComparator();
                    listaServidores.sort(compare);
                    //System.out.println("Lista Ordenada " + listaServidores);
                }

               // System.out.println("Ligações TCP ativas: " + msg.getLigacoesTCP());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

           // System.out.println("Ip: " + msg.getIp()+ "Porto: " + msg.getPortoServer());
            //System.out.println(listaServidores);
        }

    }
}



