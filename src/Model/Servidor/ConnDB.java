package Model.Servidor;

import java.sql.*;
import java.util.ArrayList;
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
                String sqlUpdateVersion = "UPDATE versao_db SET versao='" + getVersao().get()+"'";
                statement.executeUpdate(sqlUpdateVersion);
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
