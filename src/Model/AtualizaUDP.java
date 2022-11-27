package Model;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.atomic.AtomicBoolean;

public class AtualizaUDP extends Thread{
    AtomicBoolean threadCorre;
    DatagramSocket ds;

    public AtualizaUDP(DatagramSocket ds, AtomicBoolean threadCorre) {
        this.ds = ds;
        this.threadCorre = threadCorre;
    }

    @Override
    public void run() {
        DatagramPacket dp = new DatagramPacket(new byte[255],255);
        while(threadCorre.get()){
            try {
                ds.setSoTimeout(1000);
                ds.receive(dp);
                ByteArrayInputStream bais = new ByteArrayInputStream(dp.getData());
                ObjectInputStream ois = new ObjectInputStream(bais);

                Msg msg = (Msg) ois.readObject();
                System.out.println("Versao [" + msg.getVersaoBdAtualizada()+"]");
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");

    }
}
