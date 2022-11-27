package Model;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;
    ArrayList<Informacoes> listaServidores;
    static AtomicBoolean threadCorre;
    public ListenHeartBeat(MulticastSocket ms, ArrayList<Informacoes> listaServidores,AtomicBoolean threadCorre) {
        this.ms = ms;
        this.listaServidores = listaServidores;
        this.threadCorre = threadCorre;
    }

    @Override
    public void run() {
        while(threadCorre.get()) {
            DatagramPacket dp = new DatagramPacket(new byte[4000], 4000);

            try {
                ms.receive(dp);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
            ObjectInputStream ois;
            try {
                ois = new ObjectInputStream(bais);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            //Msg msg;
            Informacoes info;

            try {
                info = (Informacoes) ois.readObject();
                synchronized (listaServidores) {
                    //Informacoes info = new Informacoes(msg.getPortoServer(),msg.getIp(),msg.getLigacoesTCP());
                    if(!listaServidores.contains(info) && info.isDisponivel())
                        listaServidores.add(info);
                    else { // se já existir
                        //System.out.println("EXISTE BABY");
                        listaServidores.set(listaServidores.lastIndexOf(info),info);
                        //System.out.println("Info existente: "+ info);
                    }
                    Comparator<Informacoes> compare = new InformacoesComparator();
                    listaServidores.sort(compare);

                    //System.out.println("ListenHeartBeat: " + listaServidores);
                }
                if(info.getMsgAtualiza() != null)
                    if(info.getMsgAtualiza().equals("Prepare")) {
                        System.out.println("ListenHeartBeatAtualiza" + info);
                        enviaUDP(info.getPortoUDPAtualiza(), info.getVersaoBdAtualiza(), info.getIp());
                        int seconds = 3000;
                        ms.setSoTimeout(seconds);
                            bais = new ByteArrayInputStream(dp.getData());
                            try {
                                ois = new ObjectInputStream(bais);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            info = (Informacoes) ois.readObject();
                            if (info.getMsgAtualiza().equals("Commit")) {

                            } else if (info.getMsgAtualiza().equals("Abort")) {

                            }
                        }else if(info.getMsgAtualiza().equals("Abort")){
                        //   info.setDisponivel(true);
                    }
                    } catch (SocketException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println("[INFO] ListenHeartBeat terminado com sucesso!");
    }

    private void enviaUDP(int portoUDPAtualiza, int versaoBdAtualizada, String ip) throws IOException {
        DatagramSocket ds = new DatagramSocket();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        Msg msg = new Msg();
        msg.setVersaoBdAtualizada(versaoBdAtualizada);

        oos.writeUnshared(msg);
        byte[] messageBytes = baos.toByteArray();

        InetAddress ipServer = InetAddress.getByName(ip);

        DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portoUDPAtualiza);
        ds.send(dp);
    }
}



