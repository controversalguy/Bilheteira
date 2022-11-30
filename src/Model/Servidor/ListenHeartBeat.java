package Model.Servidor;

import utils.Msg;

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ListenHeartBeat extends Thread{
    private MulticastSocket ms;
    ArrayList<Informacoes> listaServidores;
    static AtomicBoolean threadCorre;
    ArrayList<ObjectOutputStream> listOos;
    public ListenHeartBeat(MulticastSocket ms, ArrayList<Informacoes> listaServidores,AtomicBoolean threadCorre,ArrayList<ObjectOutputStream> listOos) {
        this.ms = ms;
        this.listaServidores = listaServidores;
        this.threadCorre = threadCorre;
        this.listOos = listOos;
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
                    if (!listaServidores.contains(info)){
                        /*&& info.isDisponivel()*/
                        listaServidores.add(info);

                        for (ObjectOutputStream os: listOos) {
                            enviaListaServidoresAtualizada(os);
                        }

                    }else { // se já existir
                        //System.out.println("EXISTE BABY");
                        listaServidores.set(listaServidores.lastIndexOf(info), info);
                        //System.out.println("Info existente: "+ info);
                    }
                    Comparator<Informacoes> compare = new InformacoesComparator();
                    listaServidores.sort(compare);

                    //System.out.println("ListenHeartBeat: " + listaServidores);
                }
                if (info.getMsgAtualiza() != null) {
                    if (info.getMsgAtualiza().equalsIgnoreCase("PREPARE")) {
                        System.out.println("ListenHeartBeatAtualiza" + info);
                        enviaUDP(info.getPortoUDPAtualiza(), info.getVersaoBdAtualiza(), info.getIp());
                        while (true) {
                            ms.receive(dp);
                            bais = new ByteArrayInputStream(dp.getData());
                            try {
                                ois = new ObjectInputStream(bais);
                                info = (Informacoes) ois.readObject();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (info.getMsgAtualiza() != null) {
                                System.out.println("MSG ATUALIZA LISTEN: " + info.getMsgAtualiza());
                                if (info.getMsgAtualiza().equalsIgnoreCase("Commit")) {
                                    System.out.println("Recebi Commit");
                                    break;
                                } else if (info.getMsgAtualiza().equalsIgnoreCase("Abort")) {
                                    System.out.println("Recebi Abort");
                                    break;
                                }
                            }
                        }
                        System.out.println("RECEBA MM HEIN LISTEN");
                    }
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

        System.out.println("EU MANDEIIIIIII");

        oos.writeUnshared(msg);
        byte[] messageBytes = baos.toByteArray();

        InetAddress ipServer = InetAddress.getByName(ip);

        DatagramPacket dp = new DatagramPacket(messageBytes, messageBytes.length, ipServer, portoUDPAtualiza);
        ds.send(dp);
    }

    void enviaListaServidoresAtualizada(ObjectOutputStream oos) {
        Msg msg = new Msg();
        try {
            Iterator<Informacoes> iterator = listaServidores.iterator();

            while (iterator.hasNext()) {
                Informacoes info = iterator.next();

                msg.setPortoServer(info.getPorto());
                msg.setIp(info.getIp());
                msg.setLigacoesTCP(info.getLigacoes());
                System.out.println("MSGATUALIZA: " + msg);
                if (!iterator.hasNext()) {
                    msg.setLastPacket(true);
                }
                oos.writeUnshared(msg);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}



