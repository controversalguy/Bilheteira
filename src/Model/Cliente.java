package Model;

import java.io.*;
import java.net.*;
import java.util.*;

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
            Msg msg = new Msg(InetAddress.getLocalHost().getHostAddress(), 0);
            oos.writeUnshared(msg);
            byte[] messageBytes = baos.toByteArray();

            InetAddress ipServer = InetAddress.getByName(strIpServer);

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portServer);
            ds.send(dp);
            Msg msgTCP;

            HashMap<Integer,String> listaServidores = new HashMap<>();

            while(true){

                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                msgTCP = (Msg) ois.readObject();

                listaServidores.put(msgTCP.getPortoServer(),msgTCP.getIp());

                if(msgTCP.isLastPort())
                    break;
            }

            Iterator<Integer> portos = listaServidores.keySet().iterator();
            int portoTCP = portos.next();
            Socket sClient = new Socket("localhost", portoTCP);
            System.out.println("Connectei-me ao Servidor...["+portoTCP+"]");
            InputStream is = sClient.getInputStream();
            OutputStream os = sClient.getOutputStream();
            ObjectOutputStream oosTCP = new ObjectOutputStream(os);
            ObjectInputStream oisTCP = new ObjectInputStream(is);
            msgTCP.setMsg("Connectei ");
            oosTCP.writeUnshared(msgTCP);
            Scanner sc = new Scanner(System.in);
            while (true) {
                msgTCP.setMsg(sc.nextLine());
                System.out.println(msgTCP.getMsg());
                oosTCP.writeUnshared(msgTCP);
                msgTCP = (Msg) oisTCP.readObject();
                if (msgTCP.getMsg().equals("Tudo bem?"))
                    System.out.println("Claro né");
            }

        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
