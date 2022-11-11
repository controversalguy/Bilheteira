package Model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketException;

public class ThreadServer extends Thread {
    MulticastSocket ms ;
    int portClientes;
    public ThreadServer(MulticastSocket ms, int portClients) {
        this.ms = ms;
        this.portClientes = portClients;
    }

    @Override
    public void run() {
        //Msg msg;
        try {
            DatagramSocket ds = new DatagramSocket(portClientes);
            DatagramPacket dp = new DatagramPacket(new byte[256],256);

            ds.receive(dp);

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);

            Msg msg = (Msg) ois.readObject();
            System.out.println("Client Message "+msg.getMsg());
        } catch (SocketException e) {
            System.out.println("Erro no Socket");;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (IOException e){
            System.out.println("Erro em stream");
        }
    }
}
