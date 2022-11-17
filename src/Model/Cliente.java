package Model;

import java.io.*;
import java.net.*;
import java.util.Scanner;

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

            Msg msg = new Msg("cheguei", 0);
            oos.writeUnshared(msg);
            byte [] messageBytes = baos.toByteArray();

            InetAddress ipServer = InetAddress.getByName(strIpServer);

            DatagramPacket dp = new DatagramPacket(messageBytes,messageBytes.length,ipServer,portServer);
            ds.send(dp);
            ds.receive(dp);
            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois = new ObjectInputStream(bais);
            Msg msgTCP = (Msg) ois.readObject();
            System.out.println("Porto: " + msgTCP.getPortoTCP());
            Socket sClient = new Socket("localhost", msgTCP.getPortoTCP());
            InputStream is = sClient.getInputStream();
            OutputStream os = sClient.getOutputStream();
            ObjectOutputStream oosTCP = new ObjectOutputStream(os);
            ObjectInputStream oisTCP = new ObjectInputStream(is);
            msgTCP.setMsg("Connectei ");
            oosTCP.writeUnshared(msgTCP);
            Scanner sc = new Scanner(System.in);
            while (true){
                msgTCP.setMsg(sc.nextLine());
                //System.out.println(msgTCP.getMsg());
                oosTCP.writeUnshared(msgTCP);
                msgTCP = (Msg) oisTCP.readObject();
                if(msgTCP.getMsg().equals("Tudo bem?"))
                    System.out.println("Claro né");
            }

        }catch (UnknownHostException e){
            System.out.println("Inet address inexistente");
        }
        catch (IOException e){
            System.out.println("Erro ao aceder o MultiCastSocket");
        } catch (ClassNotFoundException e) {
            System.out.println("Msg mal lida");
        }
    }
}
