package Model.Servidor;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static Model.Servidor.Servidor.connDB;

public class ConnDB
{ // formato da data muito importante para nao dar erro
    // YYYY-MM-dd HH:mm:ss.sss
    private String DATABASE_URL;
    private Connection dbConn;
    private String dbName;
    private static AtomicInteger versaoDB;

    public ConnDB(String dBName) throws SQLException {//
        this.dbName = dBName;
        DATABASE_URL =  "jdbc:sqlite:" + dBName;
        dbConn = DriverManager.getConnection(DATABASE_URL);
        versaoDB = new AtomicInteger(1);
//        Statement statement = dbConn.createStatement();
//        String sqlQuery2 = "INSERT INTO utilizador VALUES (0,'xicao','Francisco','IS3C..00',0,0)";
//        statement.executeUpdate(sqlQuery2);
//        statement.close();
        //clear();
    }
    public boolean verificaDb() throws SQLException {
        System.out.println("[INFO] A verificar db existente...");
        ResultSet rs ;
        DatabaseMetaData meta = dbConn.getMetaData();
        rs = meta.getTables(null,null,null,new String[] {
                "TABLE"
        });
        int count = 0;
        while (rs.next()) {
            String tblName = rs.getString("TABLE_NAME");
            count++;
        }

        if(count == 6){
            return true;
        }
        return false;
    }
    public void criaTabelas()
    {
        System.out.println("[INFO] A criar tabela nova...");
        try {
            Statement statement = dbConn.createStatement();

            String sqlQueryEspetaculo = """
                    CREATE TABLE IF NOT EXISTS espetaculo
                    (id INTEGER NOT NULL,
                    descricao TEXT NOT NULL,
                    tipo TEXT NOT NULL,
                    data_hora TEXT NOT NULL,
                    duracao INTEGER NOT NULL,
                    local TEXT NOT NULL,
                    localidade TEXT NOT NULL,
                    pais TEXT NOT NULL,
                    classificacao_etaria TEXT NOT NULL,
                    visivel INTEGER NOT NULL)
                    """;

            String sqlQueryLugar = """
                    CREATE TABLE IF NOT EXISTS lugar
                    (id INTEGER NOT NULL,
                    fila TEXT NOT NULL,
                    assento TEXT NOT NULL,
                    preco REAL NOT NULL,
                    espetaculo_id INTEGER NOT NULL)
                    """;

            String sqlQueryReserva = """
                    CREATE TABLE IF NOT EXISTS reserva
                    (id INTEGER NOT NULL,
                    data_hora TEXT NOT NULL,
                    pago INTEGER NOT NULL,
                    id_utilizador INTEGER NOT NULL,
                    id_espetaculo INTEGER NOT NULL)""";

            String sqlQueryReserva_lugar = """
                    CREATE TABLE IF NOT EXISTS reserva_lugar
                    (id_reserva INTEGER NOT NULL,
                    id_lugar INTEGER NOT NULL)
                    """;

            String sqlQueryUtilizador = """
                    CREATE TABLE IF NOT EXISTS utilizador
                    (id INTEGER NOT NULL,
                    username TEXT NOT NULL,
                    nome TEXT NOT NULL,
                    password TEXT NOT NULL,
                    administrator INTEGER NOT NULL DEFAULT 0,
                    autenticado INTEGER NOT NULL DEFAULT 0 )
                    """;
            String sqlQueryVersao = """
                    CREATE TABLE IF NOT EXISTS versao_db
                    (versao INTEGER NOT NULL,
                    query TEXT NOT NULL)
                    """;

            statement.executeUpdate(sqlQueryEspetaculo);
            statement.executeUpdate(sqlQueryLugar);
            statement.executeUpdate(sqlQueryReserva);
            statement.executeUpdate(sqlQueryReserva_lugar);
            statement.executeUpdate(sqlQueryUtilizador);
            statement.executeUpdate(sqlQueryVersao);
            statement.close();


        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() throws SQLException
    {
        if (dbConn != null)
            dbConn.close();
    }

    public void listUsers(String whereName) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "SELECT id, name, birthdate FROM users";
        if (whereName != null)
            sqlQuery += " WHERE name like '%" + whereName + "%'";

        ResultSet resultSet = statement.executeQuery(sqlQuery);

        while(resultSet.next())
        {
            int id = resultSet.getInt("id");
            String name = resultSet.getString("name");
            Date birthdate = resultSet.getDate("birthdate");
            System.out.println("[" + id + "] " + name + " (" + birthdate + ")");
        }

        resultSet.close();
        statement.close();
    }

    public MensagensRetorno insertUser(ArrayList<String> msgSockett) throws SQLException
    {
        String name = msgSockett.get(1);
        String username = msgSockett.get(2);
        String password = msgSockett.get(3);

        Statement statement = dbConn.createStatement();
        String verificaExistente = "SELECT * FROM utilizador";
        if (name != null && username != null && password != null) {
            if (username.equals("admin") && password.equals("admin")){
                return MensagensRetorno.ADMIN_NAO_PODE_REGISTAR;
            }
            verificaExistente += " WHERE username = '" + username + "'";
            ResultSet resultSet = statement.executeQuery(verificaExistente);
            if (!resultSet.next()) {

                Servidor.atualiza("Prepare",connDB.getVersao().get() + 1, msgSockett);

                ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM utilizador");
                r.next();
                int count = r.getInt(1);
                r.close();

                System.out.println("TAS AQUI A FAZER O QUE");

                System.out.println("MyTable has " + count + " row(s).");
                String sqlQuery = "INSERT INTO utilizador VALUES ('" + count + "','" + username + "','" + name + "','" + password + "','" + 0 + "','" + 0 + "')";
                incrementaVersao();
                //String sqlUpdateVersion = "INSERT INTO versao_db (query)VALUES('"+sqlQuery+"')" ;
                String versao = "INSERT INTO versao_db VALUES ('" + getVersao().get() + "','" + "MUDAR" +"')"; //TODO

                statement.executeUpdate(sqlQuery);
                statement.executeUpdate(versao);
                statement.close();

                return MensagensRetorno.CLIENTE_REGISTADO_SUCESSO;
            }
        }
        statement.close();
        return  MensagensRetorno.CLIENTE_JA_REGISTADO;
    }

    public void inicializa() throws SQLException {
        Statement statement = dbConn.createStatement();
        String sqlQuery = "UPDATE utilizador SET autenticado='" + 0 + "' WHERE autenticado=" + 1;
        statement.executeUpdate(sqlQuery);

        ResultSet rs = statement.executeQuery("SELECT COUNT(*) FROM versao_db");
        rs.next();
        int aux = rs.getInt(1);
        rs.close();

        if (aux > 0) { //se ja tiver versao
            //String query = rs.getString("query");
            setVersaoDB(aux);
            System.out.println("VERSAOBD"+ getVersao());

        } else {
            // String verificaExistente = "SELECT * FROM utilizador";

            //verificaExistente += " WHERE username = '" + "admin" + "' AND password = '" + "admin" + "'";
            //ResultSet resultSet = statement.executeQuery(verificaExistente);
            //if (!resultSet.next()) {

            ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM utilizador");
            r.next();
            int count = r.getInt(1);
            r.close();
            System.out.println("MyTable has " + count + " row(s).");

            String user = "INSERT INTO utilizador VALUES ('" + count + "','" + "admin" + "','" + "admin" + "','" + "admin" + "','" + 1 + "','" + 0 + "')";
            statement.executeUpdate(user);

            //String teste = "INSERT INTO reserva_lugar VALUES ('" + 1 + "','" + 1  + "')";
            //System.out.println(teste);

            String versao = "INSERT INTO versao_db VALUES ('" + 1 + "','" + "MUDAR" +"')"; //TODO
            statement.executeUpdate(versao);
            }
        statement.close();
    }

    public String logaUser(String username, String password) throws SQLException
    {
        String login = null;
        Statement statement = dbConn.createStatement();
        String verificaExistente = "SELECT * FROM utilizador";
        if (username != null && password != null) {
            verificaExistente += " WHERE username = '" + username + "' AND password = '" + password + "'";
            ResultSet resultSet = statement.executeQuery(verificaExistente);
            if (!resultSet.next()) {
                login =  "Dados incorretos!";
            } else if (username.equals("admin") && password.equals("admin")){
                int id = resultSet.getInt("id");
                String sqlQuery = "UPDATE utilizador SET autenticado='" + 1 + "',administrator='" + 1 + "' WHERE id=" + id;
                statement.executeUpdate(sqlQuery);
                login = "Login efetuado como admin com sucesso!";

                incrementaVersao();
                String versao = "INSERT INTO versao_db VALUES ('" + getVersao().get() + "','" + "MUDAR" +"')"; //TODO
                statement.executeUpdate(versao);
            }
            else {
                int id = resultSet.getInt("id");
                String sqlQuery = "UPDATE utilizador SET autenticado='" + 1 + "' WHERE id=" + id;
                statement.executeUpdate(sqlQuery);
                login ="Login efetuado com sucesso!";

                incrementaVersao();
                String versao = "INSERT INTO versao_db VALUES ('" + getVersao().get() + "','" + "MUDAR" +"')"; //TODO
                statement.executeUpdate(versao);
            }
            resultSet.close();
        }

        statement.close();
        return login;
    }

    public String updateUser(String atualizaCampo, String id, int tipo) throws SQLException
    {
        System.out.println("SATUALIZA CAMPO:" + atualizaCampo);
        System.out.println("SATUALIZA ID:" + id);
        System.out.println("SATUALIZA CTIPO:" + tipo);
        String update = null;
        Statement statement = dbConn.createStatement();
        String sqlQuery = null;
        
        switch (tipo) {
            case 0 -> {
               sqlQuery = "UPDATE utilizador SET nome='" + atualizaCampo + "' WHERE username like '%" + id + "%'";
               update = "Nome mudado com sucesso!";
            }
            case 1 -> {
                sqlQuery = "UPDATE utilizador SET username='" + atualizaCampo + "' WHERE username like '%" + id + "%'";
                update = "Username mudado com sucesso!";
            }
            case 2 -> {
                sqlQuery = "UPDATE utilizador SET password='" + atualizaCampo + "' WHERE username like '%" + id + "%'";
                update = "Password mudada com sucesso!";
            }
        }
        
        statement.executeUpdate(sqlQuery);
        statement.close();
        return update;
    }

    public void deleteUser(int id) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "DELETE FROM users WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void clear() throws SQLException
    {
        Statement statement = dbConn.createStatement();
        String sqlQuery = "DELETE FROM espetaculo";
        statement.executeUpdate(sqlQuery);
        statement.close();

        //Getting the connection
        /*System.out.println("Connection established......");
        ResultSet rs = dbConn.getMetaData().getTables(null, null, null, null);        while (rs.next()) {
            System.out.println(rs.getString("TABLE_NAME"));
        }*/
    }

    public AtomicInteger getVersao() {
        return versaoDB;
    }

    public void incrementaVersao() {
        versaoDB.getAndIncrement();
    }
    public void setVersaoDB(int versaoDB) {
        ConnDB.versaoDB.getAndSet(versaoDB);
    }
    public void decrementaVersao() {
        versaoDB.getAndDecrement();
    }

    public String getDbName() {
        return dbName;
    }

    public String insereEspetaculos(String fileName) {
        String insere = null;

        try (FileInputStream fstream = new FileInputStream(fileName)) {

            BufferedReader br = new BufferedReader(new InputStreamReader(fstream));

            String line = br.readLine();
            String data_hora;
            ArrayList<String> listaEspetaculos = new ArrayList<>();


            HashMap<String, HashMap<String, String>> fila = new HashMap<>();

            //ADICIONA TABELA ESPETACULOS
            while (line != null) {

                String[] attributes = line.split(";");
                switch (attributes.length) {
                    case 2 -> {
                        String[] auxiliar = attributes[1].split(":");
                        if(auxiliar.length < 2)
                            listaEspetaculos.add(attributes[1]);
                    }
                    case 4 -> {
                        data_hora = attributes[1] + "-" + attributes[2] + "-" + attributes[3] + " ";

                        line = br.readLine();
                        attributes = line.split(";");

                        data_hora += attributes[1] + ":" + attributes[2];
                        listaEspetaculos.add(data_hora);
                    }
                    default -> {

                        HashMap<String, String> lugares = new HashMap<>();


                        if (attributes[0].length() == 3) {
                            // System.out.println("attributes" + Arrays.toString(attributes));
                            for (int i = 0; i < attributes.length; i++) { // corresponde a uma fila

                                String[] attributesAux = attributes[i].replace("\"", "").trim().split(":");

                                //System.out.println("attributesAux" + Arrays.toString(attributesAux));
                                if (i == 0) {
                                    attributes[0] = attributesAux[0];
                                } else if (i == 1) {
                                    if (!fila.containsKey(attributes[0])) {
                                        lugares.put(attributesAux[0], attributesAux[1]);
                                        fila.put(attributes[0], lugares);
                                    }
                                } else {
                                    if (!fila.get(attributes[0]).containsKey(attributesAux[0])) {

                                        lugares.put(attributesAux[0], attributesAux[1]);

                                        fila.get(attributes[0]).putAll(lugares);
                                    }
                                }
                            }
                          //  System.out.println("fila" + fila);
                        }
                    }

                }

                line = br.readLine();
            }

            ArrayList insereEspetaculosLista = new ArrayList<>();
            for (int i = 0; i < listaEspetaculos.size(); i++) {
                String result = listaEspetaculos.get(i).replaceAll("\"", "");
                insereEspetaculosLista.add(result);
            }

           // System.out.println("insereespetaculo"+ insereEspetaculosLista);

            Statement statement = connDB.dbConn.createStatement();

            String verificaExistente = "SELECT * FROM espetaculo";

            verificaExistente += " WHERE descricao = '" + insereEspetaculosLista.get(0)+ "' AND data_hora='" + insereEspetaculosLista.get(2)+"'";
            ResultSet resultSet = statement.executeQuery(verificaExistente);
            if (!resultSet.next()) {

                ResultSet r = statement.executeQuery("SELECT COUNT(*) FROM espetaculo");
                r.next();
                int count = r.getInt(1);
                r.close();

                String espetaculo = "INSERT INTO espetaculo VALUES ('" + count + "','" + insereEspetaculosLista.get(0) + "','" + insereEspetaculosLista.get(1) + "','" + insereEspetaculosLista.get(2) + "','" + insereEspetaculosLista.get(3) + "','"
                        + insereEspetaculosLista.get(4) + "','" + insereEspetaculosLista.get(5) + "','" + insereEspetaculosLista.get(6) + "','" + insereEspetaculosLista.get(7) + "','" + 0 + "')";

                statement.executeUpdate(espetaculo);

                for (var a: fila.keySet()) {
                    for (var b: fila.get(a).keySet()) {
                        ResultSet ra = statement.executeQuery("SELECT COUNT(*) FROM lugar");
                        r.next();
                        int count2 = ra.getInt(1);
                        r.close();
                        String insereLugar = "INSERT INTO lugar VALUES ('"+ count2 + "','"+ a +"','"+ b +"','"+ fila.get(a).get(b) +"','"+ count + "')" ;
                        statement.executeUpdate(insereLugar);
                    }
                }

            insere = "Espetáculo inserido com sucesso!";

            }
            statement.close();
            System.out.println(fila);
        } catch (IOException ioe) {
            insere = "Este ficheiro não existe!";
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return insere;
    }

    public String tornaVisivel(String id) throws SQLException {
        int idEspetaculo = Integer.parseInt(id);
        Statement statement = connDB.dbConn.createStatement();
        ResultSet r = statement.executeQuery("SELECT * FROM espetaculo WHERE id =" + idEspetaculo);
        if (r.next()) {
            String sqlQuery = "UPDATE espetaculo SET visivel='" + 1 + "' WHERE id=" + idEspetaculo;
            statement.executeUpdate(sqlQuery);
            return "Espetáculo visível!";
        }
    return "Espetáculo não existe!";
    }

    public String filtraEspetaculo(int i, String filtro)throws SQLException {

        Statement statement = connDB.dbConn.createStatement();
        ResultSet r = null;
        
        switch (i) {
            case 0 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE descricao like '%" + filtro+ "%'");
            case 1 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE tipo like '%" + filtro+ "%'");
            case 2 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE data_hora like '%" + filtro+ "%'");
            case 3 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE duracao like '%" + filtro+ "%'");
            case 4 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE local like '%" + filtro+ "%'");
            case 5 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE localidade like '%" + filtro+ "%'");
            case 6 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE pais like '%" + filtro+ "%'");
            case 7 -> r = statement.executeQuery("SELECT * FROM espetaculo WHERE classificacao_etaria like '%" + filtro+ "%'");
            case 8 -> r = statement.executeQuery("SELECT * FROM espetaculo");
        }
        
        //ResultSet r = statement.executeQuery("SELECT * FROM espetaculo WHERE descricao like '%" + filtro+ "%'");
        System.out.println("filtro:"+filtro);
        StringBuilder sb = new StringBuilder();
        StringBuilder sbs = new StringBuilder();

        while (r.next()) {
            int visibilidade = r.getInt("visivel");
            if(visibilidade == 0)
                return "Espetáculo não existente!";

            int numero = r.getInt("id");
            String descricao = r.getString("descricao");
            System.out.println("descricao"+descricao);
            String tipo = r.getString("tipo");
            String data_hora = r.getString("data_hora");
            int duracao = r.getInt("duracao");
            String local = r.getString("local");
            String localidade = r.getString("localidade");
            String pais = r.getString("pais");
            String classificacao = r.getString("classificacao_etaria");
            sbs.append("\n\nNúmero do Espetáculo: "+numero).append("\nDescrição: "+descricao).append("\nTipo: "+tipo).append("\nData: "+data_hora).append("\nDuracao: "+duracao).append("\nLocal: "+local)
                    .append("\nLocalidade: "+localidade).append("\nPais: "+pais).append("\nClassificação: "+classificacao);

        }
        if(sbs.length() != 0){
            sb = sbs;
        }else sb.append("Não existe espetáculo com esse filtro!");
        return sb.toString();
    }

    public String selecionaEspetaculo(int idEspetaculo) throws SQLException {
        LocalDateTime now = LocalDateTime.now();
        Statement statement = connDB.dbConn.createStatement();
        ResultSet r = statement.executeQuery("SELECT * FROM espetaculo WHERE id =" + idEspetaculo);
        if (r.next()) {
            String data_hora = r.getString("data_hora");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
            LocalDateTime hora_espetaculo = LocalDateTime.parse(data_hora, formatter);

            long hora = ChronoUnit.HOURS.between(now, hora_espetaculo);

            if (hora < 24)
                return "Não é possivel selecionar este espetáculo";

            int visibilidade = r.getInt("visivel");
            if (visibilidade == 0)
                return "Espetáculo não existente!";

            ResultSet rL = statement.executeQuery("SELECT * FROM lugar");
            StringBuilder sb = new StringBuilder();
            String filaaux = null;
            while (rL.next()) {
                String fila = r.getString("fila");
                if(filaaux == null)
                    filaaux = fila;
                String assento = r.getString("assento");
                String preco = r.getString("preco");
                if(!filaaux.equals(fila)){
                    sb.append("\n");
                    filaaux = null;
                }
                System.out.println("fila: " + fila);
                System.out.println("assento: " + assento);
                System.out.println("preco: " + preco);
                sb.append("|"+fila+"|").append(assento+"->").append(preco+"|");
            }

            return sb.toString();
        }
        return "Espetáculo Inexistente!";
    }

    /*public static void main(String[] args)
    {
        try
        {
            ConnDB connDB = new ConnDB();
            Scanner scanner = new Scanner(System.in);
            boolean exit = false;

            while (!exit)
            {
                System.out.print("Command: ");
                String command = scanner.nextLine();
                String[] comParts = command.split(",");

                if (command.startsWith("select"))
                    connDB.listUsers(null);
                else if (command.startsWith("find"))
                    connDB.listUsers(comParts[1]);
                else if (command.startsWith("insert"))
                    connDB.insertUser(comParts[1], comParts[2]);
                else if (command.startsWith("update"))
                    connDB.updateUser(Integer.parseInt(comParts[1]), comParts[2], comParts[3]);
                else if (command.startsWith("delete"))
                    connDB.deleteUser(Integer.parseInt(comParts[1]));
                else
                    exit = true;
            }

            connDB.close();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }*/
}
