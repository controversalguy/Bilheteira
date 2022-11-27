package Model;

import javax.management.relation.InvalidRoleInfoException;
import java.io.*;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

public class ServerSearch extends Thread {
    ArrayList<Informacoes> listaServidores;

    public ServerSearch(ArrayList<Informacoes> listaServidores) {
        this.listaServidores = listaServidores;
    }

    @Override
    public void run() {
        Msg msgTCP = new Msg();
        Iterator<Informacoes> it = listaServidores.iterator();
        Informacoes info = it.next();
        System.out.println("Porto do next: " + info.getPorto());
        Socket sClient;
        try {
            while (true) {
                try {
                    sClient = new Socket("localhost", info.getPorto());
                    break;
                } catch (ConnectException e) {
                    if (it.hasNext()) {
                        Informacoes info2 = it.next();
                        sClient = new Socket("localhost", info2.getPorto());
                        if (sClient.getPort() == info2.getPorto())
                            break;
                    } else {
                        System.out.println("FUIIIII!!!!");
                        return;
                    }
                }
            }
            System.out.println("Connectei-me ao Servidor...[" + sClient.getPort() + "]");
            InputStream is = sClient.getInputStream();
            OutputStream os = sClient.getOutputStream();
            ObjectOutputStream oosTCP = new ObjectOutputStream(os);
            ObjectInputStream oisTCP = new ObjectInputStream(is);
            msgTCP.setMsg("Connectei-me ao Servidor...[" + sClient.getPort() + "]");
            oosTCP.writeUnshared(msgTCP);
            //Scanner sc = new Scanner(System.in);
            //while (true) {
                /*msgTCP.setMsg(sc.nextLine());
                System.out.println(msgTCP.getMsg());
                oosTCP.writeUnshared(msgTCP);*/
                //msgTCP = (Msg) oisTCP.readObject();
                // (msgTCP.getMsg().equals("Tudo bem?"))
                //System.out.println(msgTCP.getMsg());
                while (true) {
                    //sc.nextLine();
                    msgTCP = (Msg) oisTCP.readObject();

                    System.out.println("ClienteMSG: " + msgTCP.toString());
                    Informacoes info1 = new Informacoes(msgTCP.getPortoServer(),msgTCP.getIp(),msgTCP.getLigacoesTCP());
                    if(!listaServidores.contains(info1))
                        listaServidores.add(info1);
                    else
                        listaServidores.set(listaServidores.lastIndexOf(info1),info1);
                    //System.out.println(msgTCP.getMsg());

                    //if (info1.isLastPacket())
                      //  break;
                }
            //}
        } catch (UnknownHostException | ClassNotFoundException e) {
            e.printStackTrace();
        } catch(SocketException e){
            ServerSearch ss = new ServerSearch(listaServidores);
            ss.start();
            try {
                ss.join();
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

