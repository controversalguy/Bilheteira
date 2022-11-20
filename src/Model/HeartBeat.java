package Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeat extends Thread  {
    int portTCP;
    InetAddress ipgroup;
    int portServers;
    MulticastSocket ms;
    String ipServer;
    AtomicInteger ligacoesTCP;

    public HeartBeat(int portTCP, InetAddress ipgroup, int portServers, MulticastSocket ms, String ipServer, AtomicInteger ligacoesTCP){
        this.portTCP = portTCP;
        this.ipgroup = ipgroup;
        this.portServers = portServers;
        this.ms = ms;
        this.ipServer = ipServer;
        this.ligacoesTCP = ligacoesTCP;
    }

    @Override
    public void run() {
        System.out.println("Welcome to the chat!["+portTCP+"]");
        while (true) {
            try {
               // System.out.println("ligacoesTCP: "+ligacoesTCP);

                Msg myMessage = new Msg(ipServer,portTCP);
                myMessage.setLigacoesTCP(ligacoesTCP.get()); // manda inteiro para não crashar
                                                            // usamos Atomic Integer pois é independente de sincronização

                //ligacoesTCP
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeUnshared(myMessage);

                byte[] myMessageBytes = baos.toByteArray();

                DatagramPacket dp = new DatagramPacket(
                        myMessageBytes, myMessageBytes.length,
                        ipgroup, portServers
                );
                ms.send(dp);

                sleep(3000); //mudar para 10 TODO
            } catch ( IOException e) {
                throw new RuntimeException(e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
