package Model;

import java.util.concurrent.atomic.AtomicBoolean;

public class AtualizaUDP extends Thread{
    private int localPort;
    AtomicBoolean threadCorre;
    public AtualizaUDP(int localPort, AtomicBoolean threadCorre) {
        this.localPort = localPort;
        this.threadCorre = threadCorre;
    }

    @Override
    public void run() {
        while(threadCorre.get()){

        }
        System.out.println("[INFO] AtualizaUDP terminado com sucesso!");
    }
}
