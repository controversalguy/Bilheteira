package Model.data;

import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadScan extends Thread {
    AtomicInteger pagamento;
    Scanner sc;
    public ThreadScan(AtomicInteger pagamento) {
        this.pagamento = pagamento;
        this.sc = new Scanner(System.in);
    }

    @Override
    public void run() {
        while (pagamento.get() != 4) {
            int opcao = sc.nextInt();

            switch (opcao) {
                case 1 -> pagamento.getAndSet(1);
                case 2 -> pagamento.getAndSet(2);
                case 3 -> pagamento.getAndSet(3);
            }

        }
    }
}
