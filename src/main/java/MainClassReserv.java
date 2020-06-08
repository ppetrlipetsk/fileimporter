import databaselib.DBSettings;
import defines.ApplicationConfig;
import defines.defaultValues;
import fileengine.FileReader;
import tableslib.TableInfo;
import throwlib.*;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MainClassReserv {
    private static Map<String,String> parameters;
    static{
        parameters=new HashMap<>();
        parameters.put("filename","");
        parameters.put("tablename","");
        parameters.put("fieldscount","");
        parameters.put("delimiter","");
        parameters.put("linedelimiter","");
        parameters.put("storealiases","");
        parameters.put("createtable","");
    }


    public static void main(String[] args) {
        if (!initLogClass()) return;
        task(args);
        loglib.Logger.closeAll();
    }

    private static void task(String[] args) {
        //if (!ConfigEngine.readConfig("config.ini")) return;
        if (!ApplicationConfig.initApplicationValues()) return;
        if (!parseProgramParameters(args)) return;

        defaultValues.setDelimiter(parameters.get("delimiter"));

        putTaskHeaderToLog();

        try {
            doIt();
        } catch (FieldTypeError fieldTypeError) {
            fieldTypeError.printStackTrace();
        }
    }

    private static void putTaskHeaderToLog() {
        loglib.Logger.getLogger("AppLog").putLine("Задание на добавление данных в БД.",true);
        loglib.Logger.getLogger("AppLog").putLine("Имя файла:"+parameters.get("filename"),true);
        loglib.Logger.getLogger("AppLog").putLine("Имя таблицы:"+parameters.get("tablename"),true);
        loglib.Logger.getLogger("AppLog").putLine("Количество полей:"+parameters.get("fieldscount"),true);
        loglib.Logger.getLogger("AppLog").putLine("Разделитель полей:"+parameters.get("delimiter"),true);
        loglib.Logger.getLogger("AppLog").putLine("Сохранять псевдонимы полей:"+parameters.get("storealiases"),true);
        loglib.Logger.getLogger("AppLog").putLine("Создавать таблицу::"+parameters.get("createtable"),true);
    }

    private static boolean initLogClass() {
        new loglib.Logger("ErrorLog","errorlog.log",2);
        new loglib.Logger("AppLog","applog.log",2);
        try {
            loglib.Logger.getLogger("ErrorLog").init();
            loglib.Logger.getLogger("AppLog").init();
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
                loglib.Logger.putLineToLogs(new String[] {"ErrorLog","AppLog"},"\"Ошибка параметра \"+par",true);
                return false;
            }
            parameters.put(par[0],par[1]);
        }

        Iterator<Map.Entry<String, String>> entries = parameters.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry<String, String> entry = entries.next();
            String s=entry.getValue();
            if ((s.length()==0)) {
                //System.out.println("Ошибка параметра "+entry.getKey());
                loglib.Logger.putLineToLogs(new String[] {"ErrorLog","AppLog"},"Ошибка параметра!",true);
                return false;
            }
        }
        return true;
    }

    private static boolean doIt() throws FieldTypeError {

        boolean result=true;

        if (!dataBaseConnection()) result=false;
        else {TableInfo tableInfo = new TableInfo(parameters.get("tablename"), parameters.get("filename"), Integer.parseInt(parameters.get("fieldscount")), Boolean.parseBoolean(parameters.get("linedelimiter")), Boolean.parseBoolean(parameters.get("storealiases")),  Boolean.parseBoolean(parameters.get("createtable")));

            loglib.Logger.getLogger("AppLog").putLine("Создание и заполнение таблицы:" + tableInfo.getTableName() + " количество полей:" + tableInfo.getFieldsCount(), true);

            //if (createTableProcess(tableInfo)) {
            if (createTable(tableInfo)){
                loglib.Logger.getLogger("AppLog").putLine("Таблица " + tableInfo.getTableName() + " создана успешно! Количество полей: " + tableInfo.getFieldsCount(), true);

                if (insertRecordsToTable(tableInfo)==-1){
                    loglib.Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog}"}, "Заполнение таблицы"+tableInfo.getTableName()+" завершено ошибкой!", true);
                }
            } else {
                loglib.Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog}"}, "Ошибка создания таблицы!", true);
                return false;
            }
        }
        return result;
    }


    private static boolean dataBaseConnection() {
        try {
            databaselib.DBEngine.connectDB(DBSettings.connectionUrl,DBSettings.userName,DBSettings.pass,DBSettings.instanceName,DBSettings.databaseName);
            loglib.Logger.getLogger("AppLog").put("Соединение с БД:"+DBSettings.instanceName+":SUCCESS\n",true);
            return true;
        }
        catch (Exception e){
            loglib.Logger.getLogger("AppLog").putLine("Ошибка подключения к БД...",true);
            loglib.Logger.getLogger("ErrorLog").putLine("Ошибка подключения к БД..."+DBSettings.instanceName);
        }
        return false;
    }

    /**
     * Считывает строки из файла, конвертирует в записи и вставляет в БД.
     * @param tableInfo
     * @return
     * @throws LineReadError
     * @throws FieldTypeError
     */
    private static int insertRecordsToTable(TableInfo tableInfo){
        loglib.Logger.getLogger("AppLog").putLine("Приступаем к добавлению записей в таблицу...",true);

        String dataLine=null;
        FileReader fileReader=new FileReader(tableInfo.getFileName());
        // пропускаем заголовок

        int indx=0;
        String[] records=null;
        try {
            fileReader.getHeaderFromFile();

            while(!fileReader.eof()){
                dataLine = getDataLineFromFile(tableInfo.getFieldsCount(),tableInfo.isLineDelimiter(),fileReader);
                if ((dataLine != null) && (dataLine.length() > 0)) {
                    records = dataLine.split(defaultValues.getDELIMITER());
         //           tableslib.TableClass.insertRecord(tableInfo.getFields(), tableInfo.getTableName(), records);
                    loglib.Logger.getLogger("AppLog").putLine("Добавление записи №"+(++indx)+" -OK",true);
                }
            }
        } catch (LineReadError lineReadError) {
            lineReadError.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        catch (SQLException e) {
//            e.printStackTrace();
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка добавления записи в БД.",true);
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Индекс строки:"+new Integer(indx).toString(),true);
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Добавляемые данные:"+records.toString(),true);
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},e.getMessage(),true);
//
//        }
////        catch (LineReadError lineReadError) {
//            lineReadError.printStackTrace();
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},lineReadError.getMessage(),true);
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка чтения строки из файла"+tableInfo.getFileName(),true);
//        } catch (IOException e) {
//            e.printStackTrace();
//            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},e.getMessage(),true);
//            //loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка чтения строки из файла"+tableInfo.getFileName(),true);
//        }
//        finally {
//            fileReader.closeReader();
//        }

        loglib.Logger.getLogger("AppLog").putLine("Добавление записей завершено. Добавлено:"+(indx)+" записей",true);
        return indx;
    }

    private static boolean createTable(TableInfo tableInfo) {

        boolean code;

        if (code=fieldsDetect(tableInfo)) {
            // loglib.Logger.getLogger("AppLog").putLine("Попытка создать таблицу:" + tableInfo.getTableName(), true);

//            if (code=tableInfo.createTable())
//                loglib.Logger.getLogger("AppLog").putLine("Таблица " + tableInfo.getTableName() + " создана успешно!");
        }
        return code;
    }

    private static boolean fieldsDetect(TableInfo tableInfo) {
        loglib.Logger.getLogger("AppLog").putLine("Процесс анализа данных таблицы начат.",true);

        FileReader fileReader=new FileReader(tableInfo.getFileName());
        int lineIndex=1; //Первая строка, не считая заголовка, уже считана
        boolean codeReturn=true;
        try {
            String header=fileReader.getHeaderFromFile();
            String dataLine=getDataLineFromFile(tableInfo.getFieldsCount(), tableInfo.isLineDelimiter(),fileReader);

            if (dataLine!=null) {
                tableInfo.preGenerateFields(header, dataLine, tableInfo.isLineDelimiter());
                System.out.println("Анализ строки №"+(lineIndex++)+" - OK");
            }
            else {
                codeReturn = false;
                throw new NullStringLineError("Ошибка в строке данных! Строка имеет нулевую длину...");
            }

            while(!fileReader.eof()){
                dataLine=getDataLineFromFile(tableInfo.getFieldsCount(),tableInfo.isLineDelimiter(),fileReader);
                if (dataLine.length()>0) {
                    System.out.println("Анализ строки №"+lineIndex);
                    tableInfo.correctFieldsType(dataLine,tableInfo.isLineDelimiter());
                }
                lineIndex++;
            }

        } catch (FieldTypeCorrectionError fieldTypeCorrectionError) {
            fieldTypeCorrectionError.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка коррекции типа поля."+fieldTypeCorrectionError.getMessage()+" строка №"+new Integer(lineIndex).toString(),true);
            codeReturn=false;
        }
        catch (NullStringLineError nullStringLineError) {
            nullStringLineError.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},nullStringLineError.getMessage()+" строка №"+new Integer(lineIndex).toString(),true);
            codeReturn=false;

        } catch (LineReadError lineReadError) {
            lineReadError.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},lineReadError.getMessage()+" строка №"+new Integer(lineIndex).toString(),true);
            codeReturn=false;
        } catch (IOException e) {
            e.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},e.getMessage()+" строка №"+new Integer(lineIndex).toString(),true);
            codeReturn=false;
        } finally {
            fileReader.closeReader();
            loglib.Logger.getLogger("AppLog").putLine("Процесс анализа данных таблицы завершен.",true);
        }

        return codeReturn;
    }

    public  static String getDataLineFromFile(int fieldsCount, boolean isLineDelimiter, FileReader fileReader) throws LineReadError, IOException, LineReadError {
        String line =fileReader.getLineFromFile();
        if ((line!="")&&!fileReader.eof()) {
            StringBuilder s=new StringBuilder();
            s.append(line);
            int fieldscount_l=isLineDelimiter?fieldsCount+1:fieldsCount;

            while (!fileReader.eof() && (fileReader.getFieldsCountFromArray(s.toString()) < fieldscount_l)) {
                s.append(fileReader.getLineFromFile());
            }

            if (fileReader.getFieldsCountFromArray(s.toString()) > fieldscount_l)
                throw new LineReadError("Ошибка чтения строки: " + s.toString());
            return s.toString();
        }
        else
            return line;
    }

}
