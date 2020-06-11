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
tabledropnonprompt -
fieldsfile - путь к файлу предопределенных значений типов полей
 */
// import table
// tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=false  createtable=false  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=true

// primary table
// tablename=zmm filename=c://files//tmc//xls//zmm.xlsx fieldscount=287 storealiases=true  createtable=true  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=false tabledropnonprompt=false

import environment.*;
import tableslib.ImportProcessor;

import java.util.Date;

import com.ppsdevelopment.loglib.Logger;

import environment.ProgramMesssages;

public class MainClass {

    public static void main(String[] args) {
        ImportProcessor importProcessor;

        if (!ApplicationInitializer.initApplication(args)) {
            System.out.println("Инициализация пограммы прошла с ошибкой!");
            ProgramMesssages.showAppParams();
        } else {
                Logger.appLog("Начало работы программы импорта: " + new Date().toString(), true);
                ProgramMesssages.putProgramParamsToLog();

                String filename= ProgramParameters.getParameterValue("filename");
                String tablename= ProgramParameters.getParameterValue("tablename");
                int fieldscount= Integer.valueOf(ProgramParameters.getParameterValue("fieldscount"));
                Boolean storealiases= Boolean.valueOf(ProgramParameters.getParameterValue("fieldscount"));
                Boolean createtable= Boolean.valueOf(ProgramParameters.getParameterValue("createtable"));
                Boolean importtable= Boolean.valueOf(ProgramParameters.getParameterValue("importtable"));
                Boolean tabledropnonprompt= Boolean.valueOf(ProgramParameters.getParameterValue("tabledropnonprompt"));

                importProcessor = new ImportProcessor(filename, tablename, fieldscount, storealiases, createtable, importtable,tabledropnonprompt);

                try {
                    importProcessor.loadFields();
                    importProcessor.loadRecordsToDataBase();
                    Logger.appLog("Импортировано:" + importProcessor.getRowCount() + " записей.", true);
                    Logger.appLog("Импорт завершен успешно.", true);
                } catch (Exception e) {
                    Logger.appLog("Импорт завершен с ошибками.", true);
                    Logger.appLog("Сообщение об ошибке:" + e, true);
                }

                Logger.appLog("Время завершения:" + new Date().toString(), true);

            }
            Logger.closeAll();
    }
}
