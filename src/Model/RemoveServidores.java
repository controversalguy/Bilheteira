package Model;

import ConnectDatabase.ConnDB;

import java.net.InetAddress;
import java.net.MulticastSocket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;

public class RemoveServidores extends Thread{
    MulticastSocket ms;
    InetAddress ipgroup;
    int portTCP;
    String ipServer;
    ArrayList<Informacoes> listaServidores;

    ConnDB connDB;


    public RemoveServidores(MulticastSocket ms, InetAddress ipgroup, int portTCP, String ipServer, ArrayList<Informacoes> listaServidores, ConnDB connDB) {
        this.ms = ms;
        this.ipgroup = ipgroup;
        this.portTCP = portTCP;
        this.ipServer = ipServer;
        this.listaServidores = listaServidores;
        this.connDB = connDB;
    }

    @Override
    public void run() {

        while (true) {

            LocalDateTime now = LocalDateTime.now();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            synchronized (listaServidores) {
                Iterator<Informacoes> it = listaServidores.iterator();

                while (it.hasNext()) {
                    Informacoes info = it.next();
                    LocalDateTime servidor = LocalDateTime.parse(info.getCurrentTime(), formatter);

                    long seconds = ChronoUnit.SECONDS.between(servidor, now);
                    if(seconds > 10){ // MUDAR PARA 35 TODO
                        System.out.println("Servidor Desconectou-se [" + info.getPorto() + "]");
                        it.remove();
                        Servidor.atualiza(ms, ipgroup, portTCP, ipServer,connDB);
                        System.out.println(listaServidores);
                    }
                }
            }

        }
    }
}
