package Model;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.Iterator;

public class ListenUDP extends Thread {
    DatagramSocket ds;
    ArrayList<Informacoes> listaServidores;
    public ListenUDP(DatagramSocket ds , ArrayList<Informacoes> listaServidores){
        this.ds = ds;
        this.listaServidores = listaServidores;
    }
    @Override
    public void run() {
        while (true){
            try{
                DatagramPacket dp = new DatagramPacket(new byte[256], 256); // recebe nome
                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                Msg msg = (Msg) ois.readObject();
                System.out.println("Client Connected[" + msg.getIp()+"]");

                Msg msgTCP = new Msg();
                msgTCP.setLastPort(false);

                Iterator<Informacoes> iterator = listaServidores.iterator();
                while (iterator.hasNext()){
                    Informacoes info = iterator.next();
                    System.out.println("Info: "+info);
                    // System.out.println("Porto: " +info);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);

                    msgTCP.setPortoServer(info.getPorto());
                    msgTCP.setIp(info.getIp());
                    if(!iterator.hasNext()){
                        msgTCP.setLastPort(true);
                    }

                    //System.out.println("Port: "+info.getPorto()+ " Ip: "+info.getIp()+ "LigacoesTCP: " + info.getLigacoes());
                    msgTCP.setLigacoesTCP(info.getLigacoes());
                    // Msg msgTCP = new Msg("Ola Sou Servidor",ss.getLocalPort());

                    oos.writeUnshared(msgTCP);
                    byte[] noCache = baos.toByteArray();
                    dp.setData(noCache, 0,noCache.length);
                    ds.send(dp);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        }
}
