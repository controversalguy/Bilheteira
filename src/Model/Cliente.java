package Model;

import java.io.*;
import java.net.*;

public class Cliente {
    static String strIpServer;
    static int portServer;
    public static void main(String[] args) {
        if(args.length != 2)
            return;
        strIpServer = args[0];
        portServer = Integer.parseInt(args[1]);

        try {
            DatagramSocket ds = new DatagramSocket();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);

            Msg msg = new Msg("cheguei");
            oos.writeObject(msg);
            byte [] messageBytes = baos.toByteArray();

            InetAddress ipServer = InetAddress.getByName(strIpServer);

            DatagramPacket dp = new DatagramPacket(messageBytes,messageBytes.length,ipServer,portServer);
            ds.send(dp);

        }catch (UnknownHostException e){
            System.out.println("Inet address inexistente");
        }
        catch (IOException e){
            System.out.println("Erro ao aceder o MultiCastSocket");
        }
    }
}
