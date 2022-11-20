package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
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

            ListenHeartBeat lhb = new ListenHeartBeat(ms, listaServidores);
            lhb.start();

            ComunicaTCP ts = null;
            int count = 0;
            DatagramSocket ds = new DatagramSocket(portClients);

            while(true){
                if(ss.isClosed())
                    break;
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

                ts = new ComunicaTCP(ms,ss,ligacoesTCP);
                ts.start();
                if(count == 20)
                    break;
                count++;
            }
            ss.close();
            ms.leaveGroup(sa, ni);
            ms.close();
            ts.join();
            hb.join();

        } catch (UnknownHostException e) {
            System.out.println("Desconhecido Host");
        } catch (IOException e) {
            System.out.println("Desconhecido");
        } catch (InterruptedException e) {
            System.out.println("InterrupcaoException");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        System.out.println("QUERO FORA");
    }
}

