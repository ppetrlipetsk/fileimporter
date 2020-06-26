package environment;

public class ApplicationGlobals {
    private static final int LINESLIMIT = 2;
    private static final String ERRORLOGName = "errorlog";
    private static final String APPLOGName = "applog";

    private static String instanceName;// = "localhost\\MSSQLSERVER";
    private static String databaseName;// = "dogc";
    private static String userName;// = "sa";
    private static String dbPassword;// = "win";
    private static final String  connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;integratedSecurity=true";

    public static int getLINESLIMIT() {
        return LINESLIMIT;
    }

    public static String getERRORLOGName() {
        return ERRORLOGName;
    }

    public static String getAPPLOGName() {
        return APPLOGName;
    }

    public static String getDBInstanceName() {
        return instanceName;
    }

    public static void setInstanceName(String instanceName) {
        ApplicationGlobals.instanceName = instanceName;
    }

    public static String getDatabaseName() {
        return databaseName;
    }

    public static void setDatabaseName(String databaseName) {
        ApplicationGlobals.databaseName = databaseName;
    }

    public static String getDBUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        ApplicationGlobals.userName = userName;
    }

    public static String getDbPassword() {
        return dbPassword;
    }

    public static void setDBPassword(String dbPassword) {
        ApplicationGlobals.dbPassword = dbPassword;
    }

    public static String getConnectionUrl() {
        return connectionUrl;
    }
}
