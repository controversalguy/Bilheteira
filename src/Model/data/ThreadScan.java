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
        while (pagamento.get() != Pagamento.LIMITE_TEMPO.ordinal()) {
            int opcao = sc.nextInt();

            switch (opcao) {
                case 1 -> pagamento.getAndSet(Pagamento.EFETUA_PAGAMENTO.ordinal());
                case 2 -> pagamento.getAndSet(Pagamento. CONSULTA_RESERVAS_PAGAS.ordinal());
                case 3 -> pagamento.getAndSet(Pagamento.CONSULTA_RESERVAS_PENDENTES.ordinal());
            }

        }
    }
}
