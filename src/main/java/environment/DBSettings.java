package environment;

public class DBSettings {
    public static String instanceName;// = "localhost\\MSSQLSERVER";
    public static String databaseName;// = "dogc";
    public static String userName;// = "sa";
    public static String pass;// = "win";
    public static String connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;integratedSecurity=true";

}
