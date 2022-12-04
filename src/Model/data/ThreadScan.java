package Model.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadScan extends Thread {
    AtomicInteger pagamento;
    BufferedReader reader;
    public ThreadScan(AtomicInteger pagamento) {
        this.pagamento = pagamento;
        reader = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        String line;
        try {
            while(!Thread.interrupted() && pagamento.get() != Pagamento.LIMITE_TEMPO.ordinal()) {
                if (!reader.ready()) {
                    try {
                        Thread.sleep(300);
                        continue;
                    } catch (InterruptedException e) {
                        //TODO: handle exception
                        //System.out.println("We got interrupted");
                        return;
                    }
                }
                line = reader.readLine();
                switch (Integer.parseInt(line)) {
                    case 1 -> pagamento.getAndSet(Pagamento.EFETUA_PAGAMENTO.ordinal());
                    case 2 -> pagamento.getAndSet(Pagamento.CONSULTA_RESERVAS_PENDENTES.ordinal());
                }
                System.out.println("Pagamento+ " + pagamento.get());
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e);
        }

      System.out.println("mm a bacano sai");
    }
}
