/*
Программа импорта файла формата XLSX в БД MS SQL.
Работает следующим образом:
Если параметр importtable=false:
Программа считывает первую строку таблицы excel, считая, что она содержит имена полей.
Далее имена полей транслитерируются. Далее идет считывание строк таблицы и определение типа поля.
Далее определенные поля, записываются в таблицу aliases, ссылаясь на запись в таблице tables (запись данной таблицы, определенной параметром tablename).
Если записи в таблице tables не существует, она создается.
Далее создается таблица с именем, определенным параметром tablename.

Если параметр importtable=true, то определение полей не происходит, запись будет производиться в таблицу с именем, определенным параметром tablename+"_import"
Далее, производится чтение исходной таблицы и запись б таблицу БД.

Значения полей:

tablename-     имя таблицы БД, в которую будет вестись запись данных. Если параметр importtable=true, то имя таблицы будет [tablename]+"_import"

filename-      путь к файлу таблицы EXCEL

fieldscount-  количество полей таблицы

storealiases- логические значение true/false. Если true, то информация о полях таблицы сохраняется в таблице aliases.

createtable-  логические значение true/false. Если true, то таблица в БД создается. При этом, если tableoverwrite=true, и таблица уже существует,
              то таблица удаляется и создается заново, на основании данных о полях таблицы с именем заданным параметром tablename, хранащихся в таблице aliases.
              Если таблица существует, и tableoverwrite=false, то таблица очищается от записей, а не создается заново.
              Если таблица не существует, а createtable=false, то таблица будет создана принудительно.


applog-       имя файла журнала приложения.
errorlog-     имя файла журнала ошибок приложения.
importtable-  логические значение true/false. Если true, то при создании таблицы БД, имя таблицы будет [tablename]+"_import".

 */
// import table
// tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=false  createtable=false  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=true

// primary table
// tablename=zmm filename=c://files//tmc//xls//zmm.xlsx fieldscount=287 storealiases=true  createtable=true  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=false tabledropnonprompt=false

import databaselib.DBSettings;
import defines.FieldsDefines;
import loglib.Logger;
import loglib.MessagesClass;
import tableslib.ImportProcessor;
import typeslib.TParameter;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class MainClass {
    public static final int LINESLIMIT = 2;
    private static final String ERRORLOGFILENAME = "errorlog.log";
    private static final String APPLOGFILENAME = "applog.log";
    private static final String ERRORLOG = "errorlog";
    private static final String APPLOG = "applog";


    private static Map<String, TParameter> parameters;

    static{
        parameters=new HashMap<>();
        parameters.put("filename",new TParameter("",true));
        parameters.put("tablename",new TParameter("",true));
        parameters.put("fieldscount",new TParameter("",true));
        //parameters.put("linedelimiter",new TParameter("",false));
        parameters.put("storealiases",new TParameter("false",false));
        parameters.put("createtable",new TParameter("false",false));
        parameters.put("importtable",new TParameter("",true));
        //parameters.put("tableoverwrite",new TParameter("",true));
        parameters.put("tabledropnonprompt",new TParameter("false",false));
        parameters.put("fieldsfile",new TParameter("",false));

        parameters.put(APPLOG,new TParameter("",true));
        parameters.put(ERRORLOG,new TParameter("",true));
    }


    public static void main(String[] args) {

        ImportProcessor importProcessor;

        if (!initAppDefines(args)) {
                System.out.println("Параметры заданы неверно!");
                showAppParams();
            return;
        }
        else
        if (dataBaseConnection())
        {
            Logger.appLog( "Начало работы программы импорта: "+new Date().toString(), true);

            putProgramParamsToLog();

            importProcessor = new ImportProcessor(parameters.get("filename").getValue(), parameters.get("tablename").getValue(),  Integer.parseInt(parameters.get("fieldscount").getValue()), Boolean.parseBoolean(parameters.get("storealiases").getValue()),Boolean.parseBoolean(parameters.get("createtable").getValue()),Boolean.valueOf(parameters.get("importtable").getValue()), Boolean.valueOf(parameters.get("tabledropnonprompt").getValue()));

            try {

                importProcessor.loadFields();
                importProcessor.loadRecordsToDataBase();
                Logger.appLog("Импортировано:"+importProcessor.getRowCount()+" записей.",true);
                //MessagesClass.putFinishTimeToLog();
                Logger.appLog("Импорт завершен успешно.",true);
            }
            catch (Exception e) {
                Logger.appLog("Импорт завершен с ошибками.",true);
                Logger.appLog("Сообщение об ошибке:"+e,true);
            }

            Logger.appLog("Время завершения:"+new Date().toString(),true);

        }
        Logger.closeAll();
    }

    private static void putProgramParamsToLog() {
        Logger.appLog("Задание на добавление данных в БД.",true);
        Logger.appLog("Имя файла:"+parameters.get("filename").getValue(),true);
        Logger.appLog("Имя таблицы:"+parameters.get("tablename").getValue(),true);
        Logger.appLog("Количество полей:"+parameters.get("fieldscount").getValue(),true);
        Logger.appLog("Сохранять псевдонимы полей:"+parameters.get("storealiases").getValue(),true);
        Logger.appLog("Создавать таблицу::"+parameters.get("createtable").getValue(),true);
        Logger.appLog("Имя файла журнала программы:"+parameters.get("applog").getValue(),true);
        Logger.appLog("Имя файла журнала ошибок:"+parameters.get("errorlog").getValue(),true);
        //Logger.appLog("Перезаписывать таблицу БД:"+parameters.get("tableoverwrite").getValue(),true);
        Logger.appLog("Импорт обновления:"+parameters.get("importtable").getValue(),true);
    }


    private static boolean initAppDefines(String[] args) {
        if (args.length == 0) {
           // MessagesClass.showAppParams();
            return false;
        } else {
            if (!parseProgramParameters(args)) return false;
            validateProgramParameters();

            if (!initLogClass()) return false;
            if (!initDefaultFields()) return false;

        }
        return true;
    }

    private static boolean initDefaultFields(){
        boolean res=true;
        if (parameters.containsKey("fieldsfile")) {
            String fileName = parameters.get("fieldsfile").getValue();
            String tableName = parameters.get("tablename").getValue();
            try {
                FieldsDefines.loadDefaultFields(fileName, tableName);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Ошибка загрузки типов полей. Ошибка:" + e.getMessage());
                loglib.Logger.getLogger(Logger.APPLOG).putLine("Ошибка загрузки типов полей. Ошибка:" + e.getMessage(), true);
                loglib.Logger.getLogger(Logger.ERRORLOG).putLine("Ошибка загрузки типов полей. Ошибка:" + e.getMessage(), true);
                res = false;
            }
        }
        return res;
    }

    private static boolean initLogClass() {
            new loglib.Logger(Logger.ERRORLOG,parameters.get(ERRORLOG).getValue(),LINESLIMIT);
            new loglib.Logger(Logger.APPLOG,parameters.get(APPLOG).getValue(), LINESLIMIT);
            try {
                loglib.Logger.getLogger(Logger.ERRORLOG).init();
                loglib.Logger.getLogger(Logger.APPLOG).init();
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
            if ((par==null)||(par.length!=2)||!parameters.containsKey(par[0])) {
                System.out.println("Ошибка параметра "+par[0]);
                return false;
            }
            parameters.get(par[0]).setValue(par[1]);
        }

        Iterator<Map.Entry<String, TParameter>> entries = parameters.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, TParameter> entry = entries.next();
            String s=entry.getValue().getValue();
            if ((s.length()==0)&&(entry.getValue().isRequire())) {
                System.out.println("Ошибка параметра "+entry.getKey());
                return false;
            }
        }
        return true;
    }
    private static void validateProgramParameters() {

        if (parameters.get("importtable").getValue().equals("true")){

            parameters.put("storealiases",new TParameter("false",false));
            parameters.put("createtable",new TParameter("true",false));

        }
    }

    private static boolean dataBaseConnection() {
        try {
            databaselib.DBEngine.connectDB(DBSettings.connectionUrl,DBSettings.userName,DBSettings.pass,DBSettings.instanceName,DBSettings.databaseName);
            Logger.appLog("Соединение с БД:"+DBSettings.instanceName+":SUCCESS\n", true);
            return true;
        }
        catch (Exception e){
            loglib.Logger.getLogger(Logger.APPLOG).putLine("Ошибка подключения к БД...",true);
            loglib.Logger.getLogger(Logger.ERRORLOG).putLine("Ошибка подключения к БД..."+DBSettings.instanceName);
        }
        return false;
    }

    public static void showAppParams() {
        String text="Программа импорта файла формата XLSX в БД MS SQL.\n" +
                "Работает следующим образом:\n" +
                "Если параметр importtable=false:\n" +
                "Программа считывает первую строку таблицы excel, считая, что она содержит имена полей.\n" +
                "Далее имена полей транслитерируются. Далее идет считывание строк таблицы и определение типа поля.\n" +
                "Далее определенные поля, записываются в таблицу aliases, ссылаясь на запись в таблице tables (запись данной таблицы, определенной параметром tablename).\n" +
                "Если записи в таблице tables не существует, она создается.\n" +
                "Далее создается таблица с именем, определенным параметром tablename.\n" +
                "\n" +
                "Если параметр importtable=true, то определение полей не происходит, запись будет производиться в таблицу с именем, определенным параметром tablename+\"_import\"\n" +
                "Далее, производится чтение исходной таблицы и запись б таблицу БД.\n" +
                "\n" +
                "Значения полей:\n" +
                "\n" +
                "tablename-     имя таблицы БД, в которую будет вестись запись данных. Если параметр importtable=true, то имя таблицы будет [tablename]+\"_import\"\n" +
                "\n" +
                "filename-      путь к файлу таблицы EXCEL\n" +
                "\n" +
                "fieldscount-  количество полей таблицы\n" +
                "\n" +
                "storealiases- логические значение true/false. Если true, то информация о полях таблицы сохраняется в таблице aliases.\n" +
                "\n" +
                "createtable-  логические значение true/false. Если true, то таблица в БД создается. При этом, если tableoverwrite=true, и таблица уже существует,\n" +
                "              то таблица удаляется и создается заново, на основании данных о полях таблицы с именем заданным параметром tablename, хранащихся в таблице aliases.\n" +
                "              Если таблица существует, и tableoverwrite=false, то таблица очищается от записей, а не создается заново.\n" +
                "              Если таблица не существует, а createtable=false, то таблица будет создана принудительно.\n" +
                "\n" +
                "\n" +
                "applog-       имя файла журнала приложения.\n" +
                "errorlog-     имя файла журнала ошибок приложения.\n" +
                "importtable-  логические значение true/false. Если true, то при создании таблицы БД, имя таблицы будет [tablename]+\"_import\".\n";

        System.out.println(text+"\n");
        System.out.println("Вызов программы:");
        System.out.println("java -jar tmc_file_importer.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount% linedelimiter=%linedelimiter%  storealiases=%storealiases%  createtable=%createtable% applog=%applog% errorlog=%errorlog%");
        System.out.println("\nПараметры программы:");
        System.out.println("tablename- имя таблицы БД, в которую будет производиться импорт данных.");
        System.out.println("filename- полный путь к файлу *.xlsx, содержащего таблицу импорта.");
        System.out.println("fieldscount- количество столбцов в таблице");
        System.out.println("linedelimiter- если true, то последнее поле будет считаться разделителем строк и не бцдет импортироваться. (true/false)");
        System.out.println("storealiases- вносить свойства полей таблицы импорта в таблицу aliases БД. (true/false) ");
        System.out.println("createtable- создавать таблицу с именем %tablename% в БД. (true/false) ");
        System.out.println("createtable- создавать таблицу с именем %tablename% в БД. (true/false) ");
        System.out.println("applog- имя файла журнала приложения \n" +
                "errorlog- имя файла журнала ошибок приложения" +
                "\n");
        System.out.println("Пример: \n" +
                "java -jar tmc_file_importer.jar tablename=zmm filename=c://files//tmc//xls//zmmeol.xlsx fieldscount=287 delimiter=@ linedelimiter=true storealiases=true createtable=true applog=zmm_applog.log errorlog=zmm_errorlog.log importtable=true tableoverwrite=true");

    }
}
