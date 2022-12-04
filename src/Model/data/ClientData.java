package Model.data;

import Model.Servidor.ClientReceiveTCP;
import Model.Servidor.Informacoes;
import utils.Msg;

import java.io.*;
import java.net.*;
import java.sql.Time;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

;import static java.lang.System.exit;

public class ClientData {
    Socket sClient;
    String ipClient;
    String cliente;
    int portUDP;
    ArrayList<Informacoes> listaServidores;
    ObjectOutputStream oos;
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
                try {
                    ds.setSoTimeout(1000);
                    ds.receive(dp);

                }catch (SocketTimeoutException e){
                    System.out.println("Nenhum servidor ativo com o porto UDP 1[" + portUDP + "]!");
                    exit(0);
                }

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

    public boolean connectaTCPServidor(AtomicInteger confirmaUpdate) {
        Informacoes info = null;
        try {
        System.out.println("ListaServersClienteAOABRIR: " + listaServidores);
        Iterator<Informacoes> it = listaServidores.iterator();
        //Informacoes info = null;

        while (it.hasNext()) {
            info = it.next();
            System.out.println("Porto do next: " + info.getPorto());

            sClient = new Socket(info.getIp(), info.getPorto());
            System.out.println("Connectei-me ao Servidor...[" + sClient.getPort() + "]");
            OutputStream os = sClient.getOutputStream();
            oos = new ObjectOutputStream(os);

            Msg msg = new Msg();
            msg.setMsg("Cliente");
            oos.writeUnshared(msg);

            Thread.UncaughtExceptionHandler h = (th, ex) -> {System.out.println(ex.getMessage());  exit(0);};
            ClientReceiveTCP crTCP = new ClientReceiveTCP(listaServidores, sClient, this, confirmaUpdate);
            crTCP.start();
            crTCP.setUncaughtExceptionHandler(h);
            return true;
        }

        } catch (ConnectException e) {
            System.out.println("Não me consegui conectar ao porto: [" + info.getPorto() + "]");

        } catch (IOException e) {
            System.out.println("IOEXCEPTION");
        }
        return false;
    }

    public boolean enviaInfo(ArrayList<String> temp) {
        try {
            if(temp.get(0).equals("LOGIN_USER"))
                cliente = temp.get(2);
            else if(temp.get(0).contains("EDITA") || temp.get(0).contains("SUBMETE_RESERVA"))
                temp.add(cliente);

            if(temp.get(0).equals("EDITA_USERNAME"))
                cliente = temp.get(1);

            System.out.println(temp);
            oos.writeUnshared(temp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    public boolean esperaPagamento(AtomicInteger pagamento) {
        ArrayList<Thread> allThreads = new ArrayList<>();
        Thread tempo = new ThreadTempo(pagamento); //TODO PASSAR VARIAVEL PARA ACABAR A THREAD CASO TENHA PAGO
        tempo.start();
        allThreads.add(tempo);
        Thread scan = new ThreadScan(pagamento);
        scan.start();
        allThreads.add(tempo);

        return false;
    }

}
