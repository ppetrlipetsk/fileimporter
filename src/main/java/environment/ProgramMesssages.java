package environment;

import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.programparameters.ProgramParameters;

public class ProgramMesssages {
    private static void appLog(String message){
        Logger.putLineToLog(ApplicationGlobals.getAPPLOGName(),message,true);
    }

    public static void putProgramParamsToLog() {
        appLog("Задание на добавление данных в БД.");
        appLog("Имя файла:"+ProgramParameters.getParameterValue("filename"));
        appLog("Имя таблицы:"+ProgramParameters.getParameterValue("tablename"));
        appLog("Количество полей:"+ProgramParameters.getParameterValue("fieldscount"));
        appLog("Сохранять псевдонимы полей:"+ProgramParameters.getParameterValue("storealiases"));
        appLog("Имя файла журнала программы:"+ProgramParameters.getParameterValue("applog"));
        appLog("Имя файла журнала ошибок:"+ProgramParameters.getParameterValue("errorlog"));
        appLog("Импорт вспомогательной таблицы:"+ProgramParameters.getParameterValue("importtable"));
        appLog("Автоподтверждение удаления таблицы:"+ProgramParameters.getParameterValue("tableoverwrite"));
    }

    public static void showAppParams() {
        String text="Программа импорта файла формата XLSX в БД MS SQL.\n" +
                "                Значения параметров приложения:\n" +
                "        tablename-     имя таблицы БД, в которую будет вестись запись данных. Если параметр importtable=true, то имя таблицы будет [tablename]+\"_import\"\n" +
                "        filename-      путь к файлу таблицы EXCEL\n" +
                "        fieldscount-  количество полей таблицы\n" +
                "        storealiases- логические значение true/false. Если true, то информация о полях таблицы сохраняется в таблице aliases.\n" +
                "        applog-       путь к файлу журнала приложения.\n" +
                "        errorlog-     путь к файлу журнала ошибок приложения.\n" +
                "                importtable-  логические значение true/false. Если true, то при создании таблицы БД, имя таблицы будет [tablename]+\"_import\", и производятся дополнительные проверки псевдонимов полей.\n" +
                "                tableoverwrite - если true, то, если создаваемая таблица уже существует в БД, она будет перезаписана. Если false, то будет выдана ошибка,\n" +
                "                и таблицу из БД следует удалить вручную. Это сделано для того, чтобы исключить случайную перезапись существующей таблицы.\n" +
                "                fieldsfile - путь к файлу предопределенных значений типов полей);\n";
        System.out.println(text);
        System.out.println("Пример: \n" +
                "java -jar tmc_file_importer.jar tablename=zmm filename=c://files//tmc//xls//zmmeol.xlsx fieldscount=287  storealiases=true  applog=zmm_applog.log errorlog=zmm_errorlog.log importtable=true tableoverwrite=true");

    }
}
