package environment;

public class ApplicationGlobals {
    private static String DELIMITER;
    private static String getDELIMITER() {
        return DELIMITER;
    }
    private static void setDelimiter(String DELIMITER) {
        ApplicationGlobals.DELIMITER = DELIMITER;
    }
    private static final int LINESLIMIT = 2;
    private static final String ERRORLOGFILENAME = "errorlog.log";
    private static final String APPLOGFILENAME = "applog.log";
    private static final String ERRORLOGName = "errorlog";
    private static final String APPLOGName = "applog";

    private static String instanceName;// = "localhost\\MSSQLSERVER";
    private static String databaseName;// = "dogc";
    private static String userName;// = "sa";
    private static String dbPassword;// = "win";
    private static String connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;integratedSecurity=true";
    private static final String[] logs={APPLOGName, ERRORLOGName};

    public static void setDELIMITER(String DELIMITER) {
        ApplicationGlobals.DELIMITER = DELIMITER;
    }

    public static String[] getLogs() {
        return logs;
    }

    public static int getLINESLIMIT() {
        return LINESLIMIT;
    }

    public static String getERRORLOGFILENAME() {
        return ERRORLOGFILENAME;
    }

    public static String getAPPLOGFILENAME() {
        return APPLOGFILENAME;
    }

    public static String getERRORLOGName() {
        return ERRORLOGName;
    }

    public static String getAPPLOGName() {
        return APPLOGName;
    }

    public static String getInstanceName() {
        return instanceName;
    }

    public static String getUserName() {
        return userName;
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

    public static void setConnectionUrl(String connectionUrl) {
        ApplicationGlobals.connectionUrl = connectionUrl;
    }
}
