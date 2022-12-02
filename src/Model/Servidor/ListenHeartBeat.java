package Model.Servidor;

import utils.Msg;

import java.io.*;
import java.net.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
            System.out.println("A ESPERA DE RECEBER");
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
                        Comparator<Informacoes> compare = new InformacoesComparator();
                        listaServidores.sort(compare);

                        for (ObjectOutputStream os: listOos) {
                            enviaListaServidoresAtualizada(os);
                        }

                    }else { // se já existir
                        //System.out.println("EXISTE BABY");
                        listaServidores.set(listaServidores.lastIndexOf(info), info);
                        System.out.println("Info existente: "+ info);

                        Comparator<Informacoes> compare = new InformacoesComparator();
                        listaServidores.sort(compare);
                    }


                    //System.out.println("ListenHeartBeat: " + listaServidores);
                }

                if (info.getMsgAtualiza() != null) {

                    ArrayList<String> msgSocket = new ArrayList<>();

                    System.out.println("LISTEN BABY " + info.getMsgAtualiza());
                    if (info.getMsgAtualiza().equalsIgnoreCase("PREPARE")) {
                        System.out.println("ListenHeartBeatAtualiza" + info);

                        msgSocket = info.getMsgSockett();

                        enviaUDP(info.getPortoUDPAtualiza(), info.getVersaoBdAtualiza(), info.getIp());
                            LocalDateTime entraWhile = LocalDateTime.now();
                        while (true) {
                            LocalDateTime atual = LocalDateTime.now();
                            long seconds = ChronoUnit.SECONDS.between(entraWhile,atual);
                            System.out.println("seconds: "+ seconds);
                            if(seconds > 2){
                                System.out.println("Esperei 3 segundos");
                                break;
                            }
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
                                    System.out.println("Recebi Commit QUERO ATUALIZAR");
                                    System.out.println(msgSocket);
                                    break;
                                } else if (info.getMsgAtualiza().equalsIgnoreCase("Abort")) {
                                    System.out.println("Recebi Abort");
                                    break;
                                }else {
                                    System.out.println("IGNORADO");
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

        System.out.println("EU enviaUDP");

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
                msg.setIndex(listaServidores.indexOf(info));
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



