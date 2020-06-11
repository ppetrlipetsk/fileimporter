package environment;

import com.ppsdevelopment.loglib.Logger;

public class ProgramMesssages {
    public static void putProgramParamsToLog() {
        Logger.appLog("Задание на добавление данных в БД.",true);
        Logger.appLog("Имя файла:"+ProgramParameters.getParameterValue("filename"),true);
        Logger.appLog("Имя таблицы:"+ProgramParameters.getParameterValue("tablename"),true);
        Logger.appLog("Количество полей:"+ProgramParameters.getParameterValue("fieldscount"),true);
        Logger.appLog("Сохранять псевдонимы полей:"+ProgramParameters.getParameterValue("storealiases"),true);
        Logger.appLog("Создавать таблицу::"+ProgramParameters.getParameterValue("createtable"),true);
        Logger.appLog("Имя файла журнала программы:"+ProgramParameters.getParameterValue("applog"),true);
        Logger.appLog("Имя файла журнала ошибок:"+ProgramParameters.getParameterValue("errorlog"),true);
        //Logger.appLog("Перезаписывать таблицу БД:"+parameters.get("tableoverwrite").getValue(),true);
        Logger.appLog("Импорт обновления:"+ProgramParameters.getParameterValue("importtable"),true);
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
