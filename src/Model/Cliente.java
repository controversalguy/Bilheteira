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

            ArrayList<Informacoes> listaServidores = new ArrayList<>();

            while(true){

                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                msgTCP = (Msg) ois.readObject();

                listaServidores.add(new Informacoes(msgTCP.getPortoServer(),msgTCP.getIp(),msgTCP.getLigacoesTCP()));

                if(msgTCP.isLastPort())
                    break;
            }
            System.out.println(listaServidores);
            Iterator<Informacoes> it = listaServidores.iterator();
            Informacoes info = it.next();
            System.out.println("Edu é gay: " + info.getPorto());
            Socket sClient = new Socket("localhost", info.getPorto());
            System.out.println("Connectei-me ao Servidor...["+info.getPorto()+"]");
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
