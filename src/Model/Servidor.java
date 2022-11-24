package Model;

import ConnectDatabase.ConnDB;

import java.io.*;
import java.net.*;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Thread.sleep;


public class Servidor {

    static final int portServers = 4004;
    static int portClients;
    static String DATASE_DIR = "./DataBase/";
    static String dBName; // TODO dbName+path
    static final String MULTICAST_IP = "239.39.39.39";
    static final AtomicInteger ligacoesTCP = new AtomicInteger(0);
    static int portServer;

    //TODO threads num ArrayList
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Argumentos inválidos {<PORT> <DBPATH> -> <9021><D:\\uni\\3ano\\1semestre\\PD\\BilheteiraGit\\DataBase>}");
            return;
        }
        ArrayList<Thread> allThreads = new ArrayList<>();
        //HashMap<Integer,String> listaServidores = new HashMap<>();
        ArrayList<Informacoes> listaServidores  = new ArrayList<>();

        // TODO VALIDAÇÕES
        portClients = Integer.parseInt(args[0]);
        dBName = args[1];
        MulticastSocket ms;

        try {
            ConnDB connDB = null;

            ms = new MulticastSocket(portServers);
            InetAddress ipgroup = InetAddress.getByName(MULTICAST_IP);
            SocketAddress sa = new InetSocketAddress(ipgroup, portServers);
            NetworkInterface ni = NetworkInterface.getByInetAddress(InetAddress.getLocalHost());
            ms.joinGroup(sa, ni);

            ServerSocket ss = new ServerSocket(0);
            portServer = ss.getLocalPort();

            String ipServer = InetAddress.getByName("localhost").getHostAddress();

            ListenHeartBeat lhb = new ListenHeartBeat(ms, listaServidores);
            lhb.start();
            allThreads.add(lhb);

            connDB = faseDeArranque(listaServidores, connDB);

            HeartBeat hb = new HeartBeat(portServer,ipgroup,portServers,ms,ipServer, ligacoesTCP);
            hb.start();
            allThreads.add(hb);

            ComunicaTCP ts;
            DatagramSocket ds = new DatagramSocket(portClients);
            ListenUDP lUDP = new ListenUDP(ds,listaServidores);
            lUDP.start();
            allThreads.add(lUDP);

            RemoveServidores rs = new RemoveServidores(ms, ipgroup, portServer, ipServer, listaServidores);
            rs.start();
            allThreads.add(rs);
            //Scanner sc = new Scanner(System.in);
            while (true){
                Socket sCli = ss.accept();
                ts = new ComunicaTCP(ms,sCli,ligacoesTCP);
                ts.start();
                allThreads.add(ts);
            }

            /*for (Thread t : allThreads) {
                t.join();
            }

            ss.close(); TODO
            ms.leaveGroup(sa, ni);
            ms.close();
            */

        } catch (UnknownHostException e) {
            System.out.println("Desconhecido Host");
        } catch (IOException e) {
            System.out.println("Desconhecido");
        }
        System.out.println("QUERO FORA");
    }

    private static ConnDB faseDeArranque(ArrayList<Informacoes> listaServidores, ConnDB connDB) {
        try {
            sleep(2000); //TODO passar para 30 segundos

            if(listaServidores.isEmpty()){
                try{
                    connDB = new ConnDB("Servidor"+ portServer+".db");
                    connDB.criaTabelas();
                }catch (SQLException e){
                    File file = new File("mydb.db");
                    file.delete();

                    System.out.println("[ERRO] A criar Base de Dados...");

                    connDB = new ConnDB("Servidor"+ portServer+".db");
                    connDB.criaTabelas();
                    return connDB;
                }

               // connDB = new ConnDB("jdbc:sqlite:Database/mydb.db",dBName);
            }else{
                int vMaior = 1;
                Iterator <Informacoes> it = listaServidores.iterator();
                System.out.println(listaServidores);
                for (int i = 0; i< listaServidores.size();i++){
                    System.out.println(listaServidores.get(i).getVersaoBd());
                    if(listaServidores.get(i).getVersaoBd()>vMaior){
                        vMaior = i; // posicao do Servidor que tem maior versao
                    }
                }

                System.out.println("vMaior: "+ vMaior);
                if(listaServidores.get(vMaior).getVersaoBd() > 1){
                    // copiar database
                    try {
                        connDB = new ConnDB("Servidor"+ listaServidores.get(vMaior).getPorto()+".db" );
                        connDB.copia("Servidor"+portServer+".db");
                    }catch (SQLException e){
                        e.printStackTrace();
                    }

                }


            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connDB;
    }

    public static void atualiza(MulticastSocket ms, InetAddress ipgroup, int portTCP, String ipServer) {
        try{
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = now.format(dateTimeFormatter);

            Informacoes info = new Informacoes(portTCP, ipServer, ligacoesTCP.get(), currentTime);
            // manda inteiro para não crashar // usamos Atomic Integer pois é independente de sincronização

            //ligacoesTCP
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeUnshared(info);

            byte[] myMessageBytes = baos.toByteArray();

            DatagramPacket dp = new DatagramPacket(
                    myMessageBytes, myMessageBytes.length,
                    ipgroup, portServers
            );
            ms.send(dp);

            System.out.println("Enviei atualizado!");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}

