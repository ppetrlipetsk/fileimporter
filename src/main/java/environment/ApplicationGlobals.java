package environment;

public class ApplicationGlobals {
    private static String DELIMITER;
    public static String getDELIMITER() {
        return DELIMITER;
    }
    public static void setDelimiter(String DELIMITER) {
        ApplicationGlobals.DELIMITER = DELIMITER;
    }
    public static final int LINESLIMIT = 2;
    public static final String ERRORLOGFILENAME = "errorlog.log";
    public static final String APPLOGFILENAME = "applog.log";
    public static final String ERRORLOG = "errorlog";
    public static final String APPLOG = "applog";

}
