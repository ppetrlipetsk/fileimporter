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

Не нужен
createtable-  логические значение true/false. Если true, то таблица в БД создается. При этом, если tableoverwrite=true, и таблица уже существует,
              то таблица удаляется и создается заново, на основании данных о полях таблицы с именем заданным параметром tablename, хранащихся в таблице aliases.
              Если таблица существует, и tableoverwrite=false, то таблица очищается от записей, а не создается заново.
              Если таблица не существует, а createtable=false, то таблица будет создана принудительно.


applog-       имя файла журнала приложения.
errorlog-     имя файла журнала ошибок приложения.
importtable-  логические значение true/false. Если true, то при создании таблицы БД, имя таблицы будет [tablename]+"_import".
tableoverwrite - если true, то, если создаваемая таблица уже существует в БД, то она будет перезаписана. Если false, то будет выдана ошибка,
              и таблицу из БД следует удалить вручную. Это сделано для того, чтобы исключить случайную перезапись существующей таблицы.
fieldsfile - путь к файлу предопределенных значений типов полей
 */
// import table
// tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=false  createtable=false  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=true

// primary table
// tablename=zmm filename=c://files//tmc//xls//zmm.xlsx fieldscount=287 storealiases=true  createtable=true  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=false tabledropnonprompt=false

/**
 * Загружается два вида таблиц (имя таблицы задается параметром "tablename"):
 * - основная таблица, которая хранится в БД и с ней ведется работа из других приложений
 * (таблица считается основной, если параметр importtable=false). Иначе, таблица считается таблицей изменений.
 * - таблица импорта изменений. Эта таблица загружается в БД с суффиксом _import, далее анализируются различия,
 * и данные из нее загружаются в основную таблицу.
 * При загрузке таблицы, о ней создается запись в таблице "tables" (если установлен параметр "createtable=true")
 * и псевдонимы полей в таблице "aliases" (если установлен параметр "storealiases=true").
 *
 * При загрузке таблицы есть следующие ситуации:
 *  1. Таблицы загружаются в чистую БД.
 *  2. При загрузке основной таблицы, другая основная таблица с тем же именем, уже загружена
 *  3. При загрузке таблицы изменений, другая таблица изменений с тем же именем, уже существует в БД.
 *  4. При загрузке таблицы изменений, основной таблицы не существует.
 *
 * Если таблица существует и требуется ее перезапись, то, для того, чтобы перезапись прошла без возникновения
 * исключения, нужно задать параметр "tableoverwrite=true".
 *  Загружаемые данные берутся из файла с именем, заданным параметром "filename"
 *  При анализе таблицы XLSX, производится чтение всей таблицы и определение типов полей.
 *  Чтобы вручную задать тип поля, следует указать wимя_поля=тип_поля в файле, указанном в параметре fieldsfile.
 *  Например: fieldsfile=zmm.ini
 */

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import environment.*;
import tableslib.Header;
import tableslib.ImportProcessor;
import com.ppsdevelopment.programparameters.ProgramParameters;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import com.ppsdevelopment.loglib.Logger;
import environment.ProgramMesssages;

/**
 * tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=true  createtable=true  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=false tableoverwrite=true fieldsfile=zmm.ini
 * tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=false  createtable=true  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=true tableoverwrite=true fieldsfile=zmm.ini
 */
public class MainClass {

    public static void main(String[] args) {
            try {
                if (!ApplicationInitializer.initApplication(args)) {
                    System.out.println("Инициализация пограммы прошла с ошибкой!");
                    ProgramMesssages.showAppParams();
                } else {
                    Logger.putLineToLog(ApplicationGlobals.getAPPLOGName(), "Начало работы программы импорта: " + new Date().toString(), true);

                    ProgramMesssages.putProgramParamsToLog();
                    ImportProcessor importProcessor=importProcessorInstance();

                    Header h=new Header();
//                    h.loadFields( ProgramParameters.getParameterValue("tablename"),
//                                                ProgramParameters.getParameterValue("filename"),
//                                                Boolean.parseBoolean(ProgramParameters.getParameterValue("importtable")),
//                                                Integer.parseInt(ProgramParameters.getParameterValue("fieldscount"))
//                    );

                    importProcessor.loadRecordsToDataBase(null);

//                    Logger.putLineToLog(ApplicationGlobals.getAPPLOGName(), "Импортировано:" + importProcessor.getRowCount() + " записей. \n Импорт завершен успешно.", true);
//                    Logger.putLineToLog(ApplicationGlobals.getAPPLOGName(), "Время завершения:" + new Date().toString(), true);
                }
            }
            catch (SQLException e) {
                e.printStackTrace();
                Logger.putLineToLog(ApplicationGlobals.getERRORLOGName(),"Ошибка уровня базы данных. Сообщение об ошибке:"+e.toString());
            }
            catch (ClassNotFoundException e) {
                Logger.putLineToLog(ApplicationGlobals.getERRORLOGName(),"Драйвер поддержки работы с базой данных не найден. Сообщение об ошибке:"+e.toString());
                e.printStackTrace();
            }
            catch (Exception e) {
                Logger.putLineToLog(ApplicationGlobals.getERRORLOGName(), "Импорт завершен с ошибками.\nСообщение об ошибке:" + e.toString(), true);
            }
//            catch (IOException e){
//                e.printStackTrace();
//                System.out.println("Ошибка инициализации системы логгирования. Аварийное завершение работы.");
//            }
            finally {
                try {
                    DataBaseConnector.close();
                    Logger.closeAll();
                } catch (SQLException e) {
                    System.out.println("Ошибка закрытия соединения с БД. Сообщение об ошибке:" + e.toString());
                }
                catch (IOException e){
                    System.out.println("Ошибка закрытия файлов журналов. Сообщение об ошибке:"+e.toString());
                }
            }
}

private static ImportProcessor importProcessorInstance(){
    String filename = ProgramParameters.getParameterValue("filename");
    String tablename = ProgramParameters.getParameterValue("tablename");
    int fieldscount = Integer.parseInt(ProgramParameters.getParameterValue("fieldscount"));
    boolean storealiases = Boolean.parseBoolean(ProgramParameters.getParameterValue("fieldscount"));
    boolean createtable = Boolean.parseBoolean(ProgramParameters.getParameterValue("createtable"));
    boolean importtable = Boolean.parseBoolean(ProgramParameters.getParameterValue("importtable"));
    boolean tableoverwrite = Boolean.parseBoolean(ProgramParameters.getParameterValue("tabledropnonprompt"));
    return new ImportProcessor(filename, tablename, fieldscount, storealiases, createtable, importtable, tableoverwrite);
}

}
