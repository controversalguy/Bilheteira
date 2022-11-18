package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.util.Scanner;


public class Servidor {

    static final int portServers = 4004;
    static int portClients;
    static String DATASE_DIR = "./DataBase/";
    static String dBName; // TODO dbName+path
    static final String MULTICAST_IP = "239.39.39.39";

    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Argumentos inválidos {<PORT> <DBPATH> -> <9021><D:\\uni\\3ano\\1semestre\\PD\\BilheteiraGit\\DataBase>}");
            return;
        }

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

            HeartBeat hb = new HeartBeat(portClients,ipgroup,portServers,ms);
            hb.start();

            ListenHeartBeat lhb = new ListenHeartBeat(ms);
            lhb.start();

            ThreadServer ts;
            int count = 0;
            DatagramSocket ds = new DatagramSocket(portClients);
            ServerSocket ss = new ServerSocket(0);
            while(true){
                DatagramPacket dp = new DatagramPacket(new byte[256], 256);
                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                Msg msg = (Msg) ois.readObject();
                System.out.println("Client Connected[" + msg.getMsg()+"]");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);

                System.out.println("Port: "+ss.getLocalPort());
                Msg msgTCP = new Msg("Olá",ss.getLocalPort());
                oos.writeUnshared(msgTCP);
                byte[] noCache = baos.toByteArray();
                dp.setData(noCache, 0,noCache.length);
                ds.send(dp);
                Socket socketCli= ss.accept();
                ts = new ThreadServer(ms, portClients,ss,socketCli);
                ts.start();
                if(count == 2)
                    break;
                count++;
            }
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

    }
}

