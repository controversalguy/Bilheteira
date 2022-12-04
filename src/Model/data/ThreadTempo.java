package Model.data;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTempo extends Thread {
    AtomicInteger pagamento;

    public ThreadTempo(AtomicInteger pagamento) {
        this.pagamento = pagamento;
    }

    @Override
    public void run() {

        LocalDateTime now = LocalDateTime.now();

        while (pagamento.get() != Pagamento.LIMITE_TEMPO.ordinal()) {
            LocalDateTime atualizado = LocalDateTime.now();
            long seconds = ChronoUnit.SECONDS.between(now, atualizado);

            if (seconds > 10) {
                pagamento.getAndSet( Pagamento.LIMITE_TEMPO.ordinal());
                break;
            }
        }

    }
}


