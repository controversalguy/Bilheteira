package Model;

import java.io.*;
import java.net.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ComunicaTCP extends Thread {
    MulticastSocket ms;
    Socket socketCli;
    AtomicInteger ligacoesTCP;

    String dbName;

    public ComunicaTCP(MulticastSocket ms, Socket socketCli,AtomicInteger ligacoesTCP, String dbName) {
        this.ms = ms;
        this.socketCli = socketCli;
        this.ligacoesTCP = ligacoesTCP;
        this.dbName = dbName;
    }

    @Override
    public void run() {
        try {
                //System.out.println("Fico a espera");
                //socketCli = ss.accept();
                //System.out.println("BOTA LUMEEEEEEEEEEE");
                ligacoesTCP.getAndIncrement();
                InputStream is = socketCli.getInputStream();
                OutputStream os = socketCli.getOutputStream();
                ObjectInputStream oisSocket = new ObjectInputStream(is);
                ObjectOutputStream oosSocket = new ObjectOutputStream(os);
                while (true){
                    Msg msgSocket = (Msg) oisSocket.readObject();

                    if(msgSocket.getMsg().equals("CloneBD")) {

                        System.out.println("Queres micar é?");

                        FileInputStream fis = new FileInputStream(dbName);
                        byte[] bufferClient = new byte[4000];
                        int nBytes;
                        do{
                            nBytes = fis.read(bufferClient);
                            Msg msg = new Msg();
                            msg.setMsgBuffer(bufferClient);
                            msg.setMsgSize(nBytes);
                            if(nBytes == -1){
                                msg.setMsgBuffer(new byte[4000]);
                                msg.setMsgSize(0);
                                msg.setLastPacket(true);
                            }else
                                msg.setLastPacket(false);
                            oosSocket.reset();
                            oosSocket.writeUnshared(msg);
                            //System.out.println("NBytes Lidos" + nBytes);
                            //out.write(bufferClient);
                        }while(nBytes != -1);

                    } else {
                        System.out.println("E" + msgSocket.getMsg());
                        oosSocket.writeUnshared(msgSocket);
                    }
                }
            } catch (ClassNotFoundException e) {
                System.out.println("Classe Não encontrada");
            } catch (IOException e) {
                try {
                    socketCli.close();
                    ligacoesTCP.getAndDecrement();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        }


}
