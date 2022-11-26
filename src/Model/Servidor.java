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
            ConnDB connDB;
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

            connDB = faseDeArranque(listaServidores);

            HeartBeat hb = new HeartBeat(portServer,ipgroup,portServers,ms,ipServer, ligacoesTCP,connDB.getVersao(),connDB.getDbName());
            hb.start();
            allThreads.add(hb);

            ComunicaTCP ts;
            DatagramSocket ds = new DatagramSocket(portClients);
            ListenUDP lUDP = new ListenUDP(ds,listaServidores);
            lUDP.start();
            allThreads.add(lUDP);

            RemoveServidores rs = new RemoveServidores(ms, ipgroup, portServer, ipServer, listaServidores,connDB);
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

    private static ConnDB faseDeArranque(ArrayList<Informacoes> listaServidores) {
        ConnDB connDB = null;
        try {
            sleep(2000); //TODO passar para 30 segundos

            if(listaServidores.isEmpty()){

                    connDB = new ConnDB(dBName);
                    connDB.criaTabelas();
                    //connDB.insertUser();

                    //connDB.criaTabelas();

                /*catch (SQLException e){
                    File file = new File("Servidor"+ portServer+".db");
                    file.delete();

                    System.out.println("[ERRO] A criar Base de Dados...");

                    connDB = new ConnDB("jdbc:sqlite:Servidor"+ portServer+".db");
                    connDB.criaTabelas();
                    return connDB;
                }*/

               // connDB = new ConnDB("jdbc:sqlite:Database/mydb.db",dBName);
            }else{
                connDB = new ConnDB(dBName);
                connDB.criaTabelas();
                connDB.decrementaVersao();

                int posMaior = -1;
                System.out.println("Lista de Servidores: " +listaServidores);
                for (int i = 0; i< listaServidores.size();i++){
                    System.out.println(listaServidores.get(i).getVersaoBd());
                    if(listaServidores.get(i).getVersaoBd()>connDB.getVersao().get()){
                        posMaior = i; // posicao do Servidor que tem maior versao
                    }
                }

                if(posMaior > - 1){
                    Socket servidorTemp = null;
                    try {
                        servidorTemp = new Socket("localhost",listaServidores.get(posMaior).getPorto());
                        System.out.println("recebaaaa");

                        InputStream is = servidorTemp.getInputStream();
                        OutputStream os = servidorTemp.getOutputStream();
                        ObjectOutputStream oosTCP = new ObjectOutputStream(os);
                        ObjectInputStream oisTCP = new ObjectInputStream(is);
                        Msg msgTCP = new Msg();
                        msgTCP.setMsg("CloneBD");
                        oosTCP.writeUnshared(msgTCP);

//                        FileInputStream fis = new FileInputStream(dBName);
//                        int nBytes;
//                        do{
//                            byte[] bufferClient = new byte[4000];
//                            nBytes = fis.read(bufferClient);
//                            System.out.println("NBytes Lidos" + nBytes);
//                            oosTCP.writeUnshared(bufferClient);
//                        }while(nBytes >= 0);


                        FileOutputStream fos = new FileOutputStream(dBName);

                        Msg msg;
                        do{

                           //nBytes = is.read(msgBuffer);
                            try {
                                msg = (Msg) oisTCP.readObject();
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }

                            fos.write(msg.getMsgBuffer(), 0, msg.getMsgSize());
                            //System.out.println(msg.getMsgSize() + " " + Arrays.toString(msg.getMsgBuffer()));

                        }while(!msg.isLastPacket());

                    } catch (IOException e) {
                        System.out.println("Não consegui aceder ao Socket do Servidor: " + servidorTemp.getPort() );
                    }

                }



                /*System.out.println("vMaior: "+ posMaior);
                if(listaServidores.get(posMaior).getVersaoBd() > 1){
                    // copiar database
                    try {
                        connDB = new ConnDB(listaServidores.get(posMaior).getDbName());
                        connDB.copia("Servidor"+portServer+".db");
                    }catch (SQLException e){
                        e.printStackTrace();
                    }
                }*/
            }

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return connDB;
    }

    public static void atualiza(MulticastSocket ms, InetAddress ipgroup, int portTCP, String ipServer, ConnDB connDB) {
        try{
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String currentTime = now.format(dateTimeFormatter);

            Informacoes info = new Informacoes(portTCP, ipServer, ligacoesTCP.get(), currentTime, connDB.getVersao().get());
            info.setDbName(connDB.getDbName());
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

