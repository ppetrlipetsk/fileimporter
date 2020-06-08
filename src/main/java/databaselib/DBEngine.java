package databaselib;
import throwlib.FieldTypeError;

import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.sql.*;

public class DBEngine {
    protected static Connection connection;


    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public static final String DRV_NAME = "com.microsoft.sqlserver.jdbc.SQLServerDriver";

    public static Connection connectDB(String url, String username, String password, String instanceName, String databaseName) throws SQLException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        Object drv=initDriver();
        if (drv!=null)
            connection = initConnection(url, username, password, instanceName, databaseName);
        else
            throw new ClassNotFoundException("Драйвер JDBC не найден!");
        return connection;
    }

    public static Object initDriver() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Object res=Class.forName(DRV_NAME).getDeclaredConstructor().newInstance();
        return res;
    }

    public static Connection initConnection(String url, String username, String password, String instanceName, String databaseName) throws SQLException {
        Connection connection = null;
        String connectionString = String.format(url, instanceName, databaseName, username, password);
        System.out.println("begin connection");
        System.out.println(connectionString);
        connection = DriverManager.getConnection(connectionString);
        System.out.println("Connection to DB succesfull!");
        return connection;
    }

    public static boolean execStatement(String expr, Connection conn, Statement statement) throws SQLException {
        boolean res = false;
            statement = conn.createStatement();
            res = statement.execute(expr);
        return res;
    }

    public static boolean execStatement(String expr) throws ConnectException, SQLException {
        Statement statement;
        boolean res = false;
        if (connection != null) {
                statement = connection.createStatement();
                res = statement.execute(expr);
        } else
            throw new ConnectException();
        return res;
    }


    public static boolean execStatement(String expr, Connection conn) throws SQLException {
        return execStatement(expr, conn, null);
    }

    public static Statement execute(Connection connection, String expr) throws SQLException {
        Statement statement = connection.createStatement();
        if (statement.execute(expr)) return statement;
           else
               return null;
    }

    public static Statement execute(String expr) throws SQLException {
        return execute(connection,expr);
    }

    /**
     * Выполняет запрос в БД и возвращает результирующий набор записей
     * @param connection - экземпляр класса Connection
     * @param expr - выражение, например: select * from tablename
     * @return
     * @throws SQLException
     */
    public static ResultSet resultExpression(Connection connection, String expr) throws SQLException {
        Statement statement;
        ResultSet resultSet = null;
        statement = connection.createStatement();
        resultSet = statement.executeQuery(expr);
        return resultSet;
    }

    public static ResultSet resultExpression(String expr) throws SQLException {
        ResultSet resultSet = resultExpression(connection,expr);
        return resultSet;
    }

    public static int getRowCount(ResultSet resultSet) throws SQLException {
            int rowCount = 0;
            resultSet.last();
            rowCount = resultSet.getRow();
            return rowCount;
    }

public static long insertQuery(String query) throws SQLException {
        String[] returnId={"id"};
        long id=-1;
            PreparedStatement statement = connection.prepareStatement(query, returnId);
            int affectedRows = statement.executeUpdate();
            if (affectedRows ==0){
                throw new SQLException("Creating user failed, no rows affected.");
            }

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    id=rs.getInt(1);
                }
            }
        return id;
    }

    public static long insertPreparedQuery(String query, IStatementFieldsSetter filler) throws SQLException, FieldTypeError {
        String[] returnId={"id"};
        long id=-1;
            PreparedStatement statement = connection.prepareStatement(query, returnId);
            filler.setValues(statement);
            int affectedRows = statement.executeUpdate();
            if (affectedRows ==0){
                throw new SQLException("Creating user failed, no rows affected.");
            }
            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    id=rs.getInt(1);
                }
            }

        return id;
    }



}