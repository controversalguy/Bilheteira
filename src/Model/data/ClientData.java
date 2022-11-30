package Model.data;

import Model.Servidor.Informacoes;
import Model.Servidor.ServerSearch;
import utils.Msg;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Iterator;

;

public class ClientData {
    Socket sClient;
    String ipClient;
    int portUDP;
    ArrayList<Informacoes> listaServidores;

    public ClientData(String ipClient, int portUDP) {
        this.ipClient = ipClient;
        this.portUDP = portUDP;
    }

    public boolean connectaServidorUDP() {
        try {
            DatagramSocket ds = new DatagramSocket();

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            Msg msg = new Msg(InetAddress.getLocalHost().getHostAddress(), 0);
            oos.writeUnshared(msg);
            byte[] messageBytes = baos.toByteArray();

            InetAddress ipServer = InetAddress.getByName(ipClient);

            DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portUDP);
            ds.send(dp);
            Msg msgTCP;

            listaServidores = new ArrayList<>();

            while (true) {

                ds.receive(dp);

                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);
                msgTCP = (Msg) ois.readObject();
                listaServidores.add(new Informacoes(msgTCP.getPortoServer(), msgTCP.getIp(), msgTCP.getLigacoesTCP()));

                if (msgTCP.isLastPacket())
                    break;
            }

            System.out.println("ListaServidores: " + listaServidores);
            ds.close();
            return true;
        } catch (SocketException e) {
            System.out.println("SOCKET EXCEPTION");
            return false;
        } catch (UnknownHostException e) {
            System.out.println("UnknownHostEXCEPTION");
            return false;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound EXCEPTION");
            return false;
        } catch (IOException e) {
            System.out.println("IOEXCEPTION");
            return false;
        }


    }

    public boolean connectaTCPServidor() {
        Iterator<Informacoes> it = listaServidores.iterator();
        Informacoes info;

        while (it.hasNext()) {
            info = it.next();
            System.out.println("Porto do next: " + info.getPorto());
            try {
                sClient = new Socket(info.getIp(), info.getPorto());
                System.out.println("Connectei-me ao Servidor...[" + sClient.getPort() + "]");
                ServerSearch ss = new ServerSearch(listaServidores, sClient, this);
                ss.start();
                return true;
            } catch (ConnectException e) {
                System.out.println("Não me consegui conectar ao porto: [" + info.getPorto() + "]");

            } catch (IOException e) {
                System.out.println("IOEXCEPTION BACANA");
            }
        }
        return false;
    }

    public boolean enviaInfo(ArrayList<String> temp) {
        try {
            OutputStream os = sClient.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);
            String a = "Olá";
            oos.reset();
            oos.writeUnshared(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }
}
