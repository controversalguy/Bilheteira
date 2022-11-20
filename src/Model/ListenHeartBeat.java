package Model;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;
    HashMap<Integer,String> listaServidores;

    public ListenHeartBeat(MulticastSocket ms, HashMap<Integer,String> listaServidores) {
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
                    listaServidores.put(msg.getPortoServer(),msg.getIp());
                    //listaServidores.add(msg.getPortoServer());
                }
                System.out.println("Ligações TCP ativas: " + msg.getLigacoesTCP());
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }

            System.out.println("Ip: " + msg.getIp()+ "Porto: " + msg.getPortoServer());
            //System.out.println(listaServidores);
        }

    }
}



