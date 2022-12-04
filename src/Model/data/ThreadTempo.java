package Model.data;

import Model.Servidor.Informacoes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTempo extends Thread {
    AtomicInteger pagamento;

    public ThreadTempo(AtomicInteger pagamento) {
        this.pagamento = pagamento;
    }

    @Override
    public void run() {

        LocalDateTime now = LocalDateTime.now();

        while (pagamento.get() != 4) {
            LocalDateTime atualizado = LocalDateTime.now();
            long seconds = ChronoUnit.SECONDS.between(now, atualizado);

            if (seconds > 10) {
                pagamento.getAndSet(4);
                break;
            }
        }

    }
}


