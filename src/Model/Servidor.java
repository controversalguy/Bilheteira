package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;


public class Servidor {

    static final int portServers = 4004;
    static int portClients;
    static String DATASE_DIR = "./DataBase/";
    static String dBName; // TODO dbName+path
    static final String MULTICAST_IP = "239.39.39.39";

    static final AtomicInteger ligacoesTCP = new AtomicInteger(0);

    //TODO threads num ArrayList
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Argumentos inválidos {<PORT> <DBPATH> -> <9021><D:\\uni\\3ano\\1semestre\\PD\\BilheteiraGit\\DataBase>}");
            return;
        }
        ArrayList<Thread> allThreads = new ArrayList<>();
        //HashMap<Integer,String> listaServidores = new HashMap<>();
        ArrayList<Informacoes> listaServidores  = new ArrayList<>();

        // TODO VALIDAÇÕES
        portClients = Integer.parseInt(args[0]);
        dBName = args[1];
        MulticastSocket ms;

        try {
            ConnDB connDB = new ConnDB(dBName);

            ms = new MulticastSocket(portServers);
            InetAddress ipgroup = InetAddress.getByName(MULTICAST_IP);
            SocketAddress sa = new InetSocketAddress(ipgroup, portServers);
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            ms.joinGroup(sa, ni);

            ServerSocket ss = new ServerSocket(0);
            int portServer = ss.getLocalPort();

            String ipServer = InetAddress.getByName("localhost").getHostAddress();

            HeartBeat hb = new HeartBeat(portServer,ipgroup,portServers,ms,ipServer, ligacoesTCP);
            hb.start();
            allThreads.add(hb);

            ListenHeartBeat lhb = new ListenHeartBeat(ms, listaServidores);
            lhb.start();
            allThreads.add(lhb);
            ComunicaTCP ts;
            DatagramSocket ds = new DatagramSocket(portClients);
            ListenUDP lUDP = new ListenUDP(ds,listaServidores);
            lUDP.start();
            allThreads.add(lUDP);

            RemoveServidores rs = new RemoveServidores(ms, ipgroup, portServer, ipServer, listaServidores);
            rs.start();
            allThreads.add(rs);
            //Scanner sc = new Scanner(System.in);
            while (true){
                Socket sCli = ss.accept();
                ts = new ComunicaTCP(ms,sCli,ligacoesTCP);
                ts.start();
                allThreads.add(ts);
            }

            /*for (Thread t : allThreads) {
                t.join();
            }

            ss.close(); TODO
            ms.leaveGroup(sa, ni);
            ms.close();
            */

        } catch (UnknownHostException e) {
            System.out.println("Desconhecido Host");
        } catch (IOException e) {
            System.out.println("Desconhecido");
        }  catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("QUERO FORA");
    }

    public static void atualiza(MulticastSocket ms, InetAddress ipgroup, int portTCP, String ipServer) {
        try{
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = now.format(dateTimeFormatter);

            Informacoes info = new Informacoes(portTCP, ipServer, ligacoesTCP.get(), currentTime);
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

            System.out.println("Enviei atualizado!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

