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
                    case 2 -> pagamento.getAndSet(Pagamento. CONSULTA_RESERVAS_PAGAS.ordinal());
                    case 3 -> pagamento.getAndSet(Pagamento.CONSULTA_RESERVAS_PENDENTES.ordinal());
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            //TODO: handle exception
            System.out.println("Ngrok exception" + e);
        }

//        while (pagamento.get() != Pagamento.LIMITE_TEMPO.ordinal()) {
//            System.out.println("esperaPagamento");
//            try {
//                System.out.println("esperaPagamento1");
//                while(sc.hasNextInt()){
//                    int opcao = sc.nextInt();
//                    System.out.println("esperaPagamento2");
//                    switch (opcao) {
//                        case 1 -> {
//                            pagamento.getAndSet(Pagamento.EFETUA_PAGAMENTO.ordinal());
//                            return;
//                        }
//                        case 2 -> pagamento.getAndSet(Pagamento. CONSULTA_RESERVAS_PAGAS.ordinal());
//                        case 3 -> pagamento.getAndSet(Pagamento.CONSULTA_RESERVAS_PENDENTES.ordinal());
//                    }
//                }
//            }catch (Exception e){
//                System.out.println("mm a bacano");
//                return;
//            }
//        }
        System.out.println("mm a bacano sai");
    }
}
