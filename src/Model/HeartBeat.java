package Model;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class HeartBeat extends Thread  {
    String dbName;
    AtomicInteger versaoDB;
    int portTCP;
    InetAddress ipgroup;
    int portServers;
    MulticastSocket ms;
    String ipServer;
    AtomicInteger ligacoesTCP;

    public HeartBeat(int portTCP, InetAddress ipgroup, int portServers, MulticastSocket ms,
                     String ipServer, AtomicInteger ligacoesTCP, AtomicInteger versao, String dbName){
        this.portTCP = portTCP;
        this.ipgroup = ipgroup;
        this.portServers = portServers;
        this.ms = ms;
        this.ipServer = ipServer;
        this.ligacoesTCP = ligacoesTCP;
        this.versaoDB = versao;
        this.dbName = dbName;
    }

    @Override
    public void run() {
        System.out.println("Welcome to the chat!["+portTCP+"]");
        while (true) {
            try {

                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String currentTime = now.format(dateTimeFormatter);

                Informacoes info = new Informacoes(portTCP, ipServer, ligacoesTCP.get(),currentTime);
                info.setVersaoBd(versaoDB.get());
                info.setDbName(dbName);
                System.out.println(info);
                // manda inteiro para não crashar // usamos Atomic Integer pois é independente de sincronização
                //ligacoesTCP
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                oos.writeUnshared(info);

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
