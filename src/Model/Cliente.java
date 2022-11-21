package Model;

import java.io.*;
import java.net.*;
import java.util.*;

public class Cliente {
    static String strIpServer;
    static int portServer;
    static ArrayList<Informacoes> listaServidores;

    public static void main(String[] args) throws IOException, InterruptedException {
        if (args.length != 2)
            return;
        strIpServer = args[0];
        portServer = Integer.parseInt(args[1]);

        try {
            DatagramSocket ds = new DatagramSocket();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            Msg msg = new Msg(InetAddress.getLocalHost().getHostAddress(), 0);
            oos.writeUnshared(msg);
            byte[] messageBytes = baos.toByteArray();

            InetAddress ipServer = InetAddress.getByName(strIpServer);

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portServer);
            ds.send(dp);
            Msg msgTCP;

            listaServidores = new ArrayList<>();

            while (true) {

                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                msgTCP = (Msg) ois.readObject();
                listaServidores.add(new Informacoes(msgTCP.getPortoServer(), msgTCP.getIp(), msgTCP.getLigacoesTCP()));

                if (msgTCP.isLastPort())
                    break;
            }

            ds.close();
            System.out.println(listaServidores);
            ServerSearch ss = new ServerSearch(listaServidores);
            ss.start();
            ss.join();
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
