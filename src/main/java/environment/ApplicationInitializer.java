package environment;

import com.ppsdevelopment.configlib.ConfigReader;
import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsDefaults;
import com.ppsdevelopment.programparameters.ProgramParameters;
import com.ppsdevelopment.programparameters.ProgramParameter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Map;

public class ApplicationInitializer {
    private static String[] logs={ApplicationGlobals.getAPPLOGName(),ApplicationGlobals.getERRORLOGName()};


    public String[] getLogs() {
        return logs;
    }

    public void setLogs(String[] logs) {
        logs = logs;
    }

    public static boolean initApplication(String[] args) throws IOException, Exception {
        initProgramParametersValues();
        if (!importProgramParameters(args)) return false;
        if (!importConfigParameters()) return false;
        initLogger();
        loadFieldsDefaultTypes();
        dataBaseConnection();
        return true;
    }

    private static void initLogger() throws IOException {
       new Logger(ApplicationGlobals.getERRORLOGName(),ProgramParameters.getParameterValue(ApplicationGlobals.getERRORLOGName()),ApplicationGlobals.getLINESLIMIT());
       new Logger(ApplicationGlobals.getAPPLOGName(),ProgramParameters.getParameterValue(ApplicationGlobals.getAPPLOGName()), ApplicationGlobals.getLINESLIMIT());
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

    private static void  loadFieldsDefaultTypes() throws IOException, SQLException, ClassNotFoundException {
        if (ProgramParameters.parameterExists("fieldsfile")) {
            String fileName = ProgramParameters.getParameterValue("fieldsfile");
            String tableName = ProgramParameters.getParameterValue("tablename");
            try {
                FieldsDefaults.loadDefaultFields(fileName, tableName);
            } catch (IOException e) {
                throw new IOException("Ошибка загрузки информации о предопределенных типах полей. Сообщение об ошибке:"+e.toString());
            }
        }
    }

    private static void dataBaseConnection() throws Exception {
        try {
            DataBaseConnector.connectDataBase(ApplicationGlobals.getConnectionUrl(),ApplicationGlobals.getDBUserName(),ApplicationGlobals.getDbPassword(),ApplicationGlobals.getDBInstanceName(),ApplicationGlobals.getDatabaseName());
        } catch (SQLException|ClassNotFoundException e) {
          throw new Exception("Ошибка подключения к БД..."+ApplicationGlobals.getDBInstanceName()+" Сообщение об ошибке:"+e.toString());
        }
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
        ProgramParameters.setParameterProperties(ApplicationGlobals.getAPPLOGName(),"",true);
        ProgramParameters.setParameterProperties(ApplicationGlobals.getERRORLOGName(),"",true);
    }

}
