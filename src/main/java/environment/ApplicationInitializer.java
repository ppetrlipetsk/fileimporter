package environment;

import com.ppsdevelopment.configlib.ConfigReader;
import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldsDefaults;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ApplicationInitializer {
    private String[] logs;

    public String[] getLogs() {
        return logs;
    }

    public void setLogs(String[] logs) {
        this.logs = logs;
    }

    public static boolean initApplication(String[] args) {
        initProgramParametersValues();
        if (!importProgramParameters(args)) return false;
        if (!importConfigParameters()) return false;
        if (!initLogClass()) return false;
        if (!loadFieldsDefaultTypes()) return false;
        if (!dataBaseConnection()) return false;

        return true;
    }

    private static boolean importConfigParameters() {
        if (!com.ppsdevelopment.configlib.ConfigReader.readConfig("config.ini")) return false;
        ApplicationGlobals.setDatabaseName(ConfigReader.getPropertyValue("databaseName"));
        ApplicationGlobals.setInstanceName(ConfigReader.getPropertyValue("instanceName"));
        ApplicationGlobals.setDBPassword(ConfigReader.getPropertyValue("password"));
        ApplicationGlobals.setUserName(ConfigReader.getPropertyValue("userName"));
        return true;
    }

    private static boolean importProgramParameters(String[] args) {
        if ((args.length == 0)|| (!parseProgramParameters(args))) return false;
            prepareProgramParameters();
        return true;
    }

    private static boolean initLogClass() {
        new Logger(Logger.ERRORLOG,ProgramParameters.getParameterValue(ApplicationGlobals.getERRORLOG()),ApplicationGlobals.getLINESLIMIT());
        new Logger(Logger.APPLOG,ProgramParameters.getParameterValue(ApplicationGlobals.getAPPLOG()), ApplicationGlobals.getLINESLIMIT());
        try {
            Logger.getLogger(Logger.ERRORLOG).init();
            Logger.getLogger(Logger.APPLOG).init();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Ошибка инициализации системы логгирования. Аварийное завершение работы.");
            return false;
        }
        return true;
    }

    private static boolean parseProgramParameters(String[] args) {
        for( String arg:args){
            String[] par=arg.split("=");
            if ((par==null)||(par.length!=2)||!ProgramParameters.parameterExists(par[0])) {
                System.out.println("Ошибка параметра "+par[0]);
                return false;
            }
            ProgramParameters.setParameterValue(par[0],par[1]);
        }

        Iterator<Map.Entry<String, ProgramParameter>> entries = ProgramParameters.getIterator();
        while (entries.hasNext()) {
            Map.Entry<String, ProgramParameter> entry = entries.next();
            ProgramParameter p=entry.getValue();
            String s=p.getValue();
            if ((s.length()==0)&&(p.isRequire())) {
                System.out.println("Ошибка параметра "+entry.getKey());
                return false;
            }
        }
        return true;
    }

    private static void prepareProgramParameters() {
        if (ProgramParameters.getParameterValue("importtable").equals("true")){
            ProgramParameters.setParameterProperties("storealiases","false",false);
            ProgramParameters.setParameterProperties("createtable","true",false);
        }
    }

    private static boolean loadFieldsDefaultTypes(){
        boolean result=true;
        if (ProgramParameters.parameterExists("fieldsfile")) {
            String fileName = ProgramParameters.getParameterValue("fieldsfile");
            String tableName = ProgramParameters.getParameterValue("tablename");
            try {
                FieldsDefaults.loadDefaultFields(fileName, tableName);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка загрузки типов полей. Ошибка:" + e.getMessage());
                Logger.getLogger(Logger.APPLOG).putLine("Ошибка загрузки типов полей. Ошибка:" + e.getMessage(), true);
                Logger.getLogger(Logger.ERRORLOG).putLine("Ошибка загрузки типов полей. Ошибка:" + e.getMessage(), true);
                result = false;
            }
        }
        return result;
    }

    private static boolean dataBaseConnection() {
        try {

            DataBaseConnector.connectDataBase(ApplicationGlobals.getConnectionUrl(),ApplicationGlobals.getDBUserName(),ApplicationGlobals.getDbPassword(),ApplicationGlobals.getDBInstanceName(),ApplicationGlobals.getDatabaseName());
            Logger.appLog("Соединение с БД:"+ApplicationGlobals.getDBInstanceName()+":SUCCESS\n", true);
            return true;
        }
        catch (Exception e){
            Logger.getLogger(Logger.APPLOG).putLine("Ошибка подключения к БД...",true);
            Logger.getLogger(Logger.ERRORLOG).putLine("Ошибка подключения к БД..."+ApplicationGlobals.getDBInstanceName());
        }
        return false;
    }

    private static void initProgramParametersValues(){
        ProgramParameters.setParameterProperties("filename","",true);
        ProgramParameters.setParameterProperties("tablename","",true);
        ProgramParameters.setParameterProperties("fieldscount","",true);
        ProgramParameters.setParameterProperties("storealiases","false",false);
        ProgramParameters.setParameterProperties("createtable","false",false);
        ProgramParameters.setParameterProperties("importtable","",true);
        ProgramParameters.setParameterProperties("tabledropnonprompt","false",false);
        ProgramParameters.setParameterProperties("fieldsfile","",false);
        ProgramParameters.setParameterProperties(ApplicationGlobals.getAPPLOG(),"",true);
        ProgramParameters.setParameterProperties(ApplicationGlobals.getERRORLOG(),"",true);
    }
}
