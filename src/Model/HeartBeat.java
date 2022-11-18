package Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class HeartBeat extends Thread  {
    int portClients;
    InetAddress ipgroup;
    int portServers;
    MulticastSocket ms;
    public HeartBeat(int portClients, InetAddress ipgroup, int portServers, MulticastSocket ms){
        this.portClients = portClients;
        this.ipgroup = ipgroup;
        this.portServers = portServers;
        this.ms = ms;
    }

    @Override
    public void run() {
        System.out.println("Welcome to the chat!");
        Scanner sc = new Scanner(System.in);
        while (true) {
            String msg = sc.nextLine();
            Msg myMessage = new Msg(msg, portClients);
            if(myMessage.getMsg().equals("exit"))
                break;
            try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(myMessage);

            byte[] myMessageBytes = baos.toByteArray();

            DatagramPacket dp = new DatagramPacket(
                    myMessageBytes, myMessageBytes.length,
                    ipgroup, portServers
            );
                ms.send(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
