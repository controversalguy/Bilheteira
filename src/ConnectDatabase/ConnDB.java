package ConnectDatabase;

import java.sql.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnDB
{ // formato da data muito importante para nao dar erro
    // YYYY-MM-dd HH:mm:ss.sss
    private String DATABASE_URL;
    private Connection dbConn;
    private int versao;
    private String dbName;

    static AtomicInteger versaoDB;

    public ConnDB(String dBName) throws SQLException {//
        this.dbName = dBName;
        DATABASE_URL =  "jdbc:sqlite:" + dBName;
        System.out.println(DATABASE_URL);
        dbConn = DriverManager.getConnection(DATABASE_URL);
        versaoDB = new AtomicInteger(1);
        //clear();
    }

    public void copia(String database_dir) throws SQLException {
        Statement statement = dbConn.createStatement();
        String sqlQuery1 = "ATTACH DATABASE '"+database_dir+"' AS backup";
        //String sqlQuery2 = "CREATE TABLE backup.utilizador as SELECT * FROM main.utilizador";
        String sqlQuery2 = "CREATE TABLE backup.utilizador as SELECT * FROM main.utilizador";
        //String sqlQuery2 = "INSERT INTO backup.utilizador VALUES (0,'xicao','Francisco','IS3C..00',0,0)";
        //String sqlQuery2 = "DELETE FROM backup.utilizador";
        //String sqlQuery4 = "INSERT INTO backup.espetaculo VALUES (4,'descricao','tipo','data_hora',2,'local','localidade','pais'" +
        //         ",'classificacao_etaria',1)";
        String sqlQuery4 = "INSERT INTO backup.utilizador VALUES (10,'asdsad','213asd','asdsad',0,0)";
        statement.executeUpdate(sqlQuery1);
        //statement.executeUpdate(sqlQuery4);
        statement.executeUpdate(sqlQuery2);
        statement.executeUpdate(sqlQuery4);

        statement.close();
    }
    public void criaTabelas()
    {
        System.out.println("[INFO] A criar tabela nova...");
        try {
            Statement statement = dbConn.createStatement();

            String sqlQueryEspetaculo = "CREATE TABLE IF NOT EXISTS espetaculo" +
                    "(id INTEGER NOT NULL," +
                    "descricao TEXT NOT NULL," +
                    "tipo TEXT NOT NULL," +
                    "data_hora TEXT NOT NULL," +
                    "duracao INTEGER NOT NULL," +
                    "local TEXT NOT NULL," +
                    "localidade TEXT NOT NULL," +
                    "pais TEXT NOT NULL," +
                    "classificacao_etaria TEXT NOT NULL," +
                    "visivel INTEGER NOT NULL)";

            String sqlQueryLugar = "CREATE TABLE IF NOT EXISTS lugar" +
                    "(id INTEGER NOT NULL," +
                    "fila TEXT NOT NULL," +
                    "assento TEXT NOT NULL," +
                    "preco REAL NOT NULL," +
                    "espetaculo_id INTEGER NOT NULL)";

            String sqlQueryReserva = "CREATE TABLE IF NOT EXISTS reserva" +
                    "(id INTEGER NOT NULL," +
                    "data_hora TEXT NOT NULL," +
                    "pago INTEGER NOT NULL," +
                    "id_utilizador INTEGER NOT NULL," +
                    "id_espetaculo INTEGER NOT NULL)";

            String sqlQueryReserva_lugar = "CREATE TABLE IF NOT EXISTS reserva_lugar" +
                    "(id_reserva INTEGER NOT NULL," +
                    "id_lugar INTEGER NOT NULL)";

            String sqlQueryUtilizador = "CREATE TABLE IF NOT EXISTS utilizador" +
                    "(id INTEGER NOT NULL," +
                    "username TEXT NOT NULL," +
                    "nome TEXT NOT NULL," +
                    "password TEXT NOT NULL," +
                    "administrator INTEGER NOT NULL DEFAULT 0," +
                    "autenticado INTEGER NOT NULL DEFAULT 0 )";

            statement.executeUpdate(sqlQueryEspetaculo);
            statement.executeUpdate(sqlQueryLugar);
            statement.executeUpdate(sqlQueryReserva);
            statement.executeUpdate(sqlQueryReserva_lugar);
            statement.executeUpdate(sqlQueryUtilizador);

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

    public void insertUser(String name, String birthdate) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "INSERT INTO utilizador VALUES (NULL,'" + name + "','" + birthdate + "')";
        statement.executeUpdate(sqlQuery);
        statement.close();
    }

    public void updateUser(int id, String name, String birthdate) throws SQLException
    {
        Statement statement = dbConn.createStatement();

        String sqlQuery = "UPDATE users SET name='" + name + "', " +
                "BIRTHDATE='" + birthdate + "' WHERE id=" + id;
        statement.executeUpdate(sqlQuery);
        statement.close();
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
