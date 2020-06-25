import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldType;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsDefaults;
import environment.ApplicationGlobals;
import org.junit.jupiter.api.Test;
import tableslib.TableLib;
import tableslib.header.HeaderCaller;
import tableslib.header.HeaderTestHelper;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Version 1.0.1
 */

class HeaderTest {

    private static void dataBaseConnection() throws Exception {
        String instanceName = "localhost\\MSSQLSERVER";
        String databaseName = "dogc";
        String userName = "sa";
        String password = "win";
        String connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;integratedSecurity=true";

        try {
            DataBaseConnector.connectDataBase(connectionUrl,userName,password,instanceName,databaseName);
        } catch (SQLException|ClassNotFoundException e) {
            throw new Exception("Ошибка подключения к БД..."+ApplicationGlobals.getDBInstanceName()+" Сообщение об ошибке:"+e.toString());
        }
    }

    private static final String TABLENAME="example1";
    static {
        try {
            dataBaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Проверяем выполнение:
    // - создание таблицы
    // - создание записи таблицы в таблице tables
    // - создание псевдонимов (набор полей, их имена, количество, соответствие типов заданномй коллекции)
    //- соответствие имен полей таблицы БД, заданному набору эталонной коллекции.

    //TODO Сделать проверку работы импорта предопределенных полей

    @Test
    void loadFields() {
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            TableLib.dropTableIfExists(TABLENAME);
            headerCaller.loadFieldsStandartCall();

            actual= h.checkTableCreate(TABLENAME)
                    && h.checkTableRecordCreated(TABLENAME)
                    && h.checkAliasesCount(TABLENAME)
                    && h.checkTableFieldsCount(TABLENAME)
                    && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }

        System.out.println("LoadFields1:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }


    // Задаем неверное имя файла
    // Expected: Исключение, с сообщением о неверном имени файла
    @Test
    void loadFieldsBadFileName() {
        // Задаем неправильное имя файла. Должно быть исключение и выход.
        boolean result;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            TableLib.dropTableIfExists(TABLENAME);
            headerCaller.loadFieldsIllegalFileName();

            result = h.checkTableCreate(TABLENAME)
                        && h.checkTableRecordCreated(TABLENAME)
                        && h.checkAliasesCount(TABLENAME)
                        && h.checkTableFieldsCount(TABLENAME)
                        && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            result=false;
        }

        System.out.println("loadFieldsBadFileName:"+(result?"OK":"FAILED"));
        assertFalse(result);
    }


    // Неверное количество полей. Больше, чем нужно.
    // Expected: В этом случае должны создаться дополнительные поля с именами fields...
    @Test
    void loadFieldsIllegalFieldCount() {
        System.out.println("Проверка вызова при количестве полей большем, чем нужно.");
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            h.getFIELDSSET().put("field",FieldType.STRINGTYPE);
            h.getFIELDSSET().put("field0",FieldType.STRINGTYPE);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            TableLib.dropTableIfExists(TABLENAME);
            headerCaller.loadFieldsOverFieldsCount();

            actual= h.checkTableCreate(TABLENAME)
                    && h.checkTableRecordCreated(TABLENAME)
                    && h.checkAliasesCount(TABLENAME)
                    && h.checkTableFieldsCount(TABLENAME)
                    && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        System.out.println("LoadFieldsIllegalFieldsCount:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }


    // Задаем меньшее количество полей, меньше, чем есть в таблице XSLX
    // Должны получить таблицу с набором полей, меньшим, чем в эталонном наборе данных (коллекции)
    @Test
     void loadFieldsIllegalFieldCountLessThanNeed() {
        System.out.println("Проверка: параметр, задающий количество полей в таблице XLSX, задан меньше, чем в эталонной коллекции");
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            h.getFIELDSSET().put("field",FieldType.STRINGTYPE);
            h.getFIELDSSET().put("field0",FieldType.STRINGTYPE);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            TableLib.dropTableIfExists(TABLENAME);
            headerCaller.loadFieldsLessFieldCount();

            actual= h.checkTableCreate(TABLENAME)
                    && h.checkTableRecordCreated(TABLENAME)
                    && !h.checkAliasesCount(TABLENAME)
                    && !h.checkTableFieldsCount(TABLENAME)
                    && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        System.out.println("loadFieldsIllegalFieldCountLessThanNeed:"+(!actual?"OK":"FAILED"));
        assertFalse(actual);
    }


    // Создаем таблицу, не сохраняем псевдонимы
    // Должны получить созданную теблицу, без псевдонимов и записи в таблице tables.
    @Test
     void loadFieldsNoStoreAliases() {
        System.out.println("Проверка: параметр, storeAliases=false");
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            TableLib.dropTableIfExists(TABLENAME);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            headerCaller.loadFieldsNoStoreAliases();

            actual= h.checkTableCreate(TABLENAME)
                    && !h.checkTableRecordCreated(TABLENAME)
                    && h.getDBAliasesCount(TABLENAME)==0
                    && h.checkTableFieldsCount(TABLENAME)
                    && !h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);

        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        if (actual) System.out.println("Количество записей в таблице псевдонимов=0");
        System.out.println("loadFieldsNoStoreAliases:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }


    // Проверим, поведение, если таблица БД существует, а параметр TableOverwrite=false
    // Должны получить сообщение о нехватке прав на удаление существующей таблицы.
    @Test
     void loadFieldsNoTableOverwrite() {
        System.out.println("Проверка: параметр, TableOverwrite=false:");
        boolean actual;
        //initFieldsSet();
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            TableLib.dropTableIfExists(TABLENAME);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            headerCaller.createDBTable(TABLENAME);
            headerCaller.loadFieldsNoTableOverwrite(h.getFIELDS().length);

            actual= !h.checkTableCreate(TABLENAME)
                    && !h.checkTableRecordCreated(TABLENAME)
                    && h.getDBAliasesCount(TABLENAME)==0
                    && !h.checkTableFieldsCount(TABLENAME)
                    && !h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);

        } catch (Exception e) {
            System.out.println(e.toString()); // тут надо проверить, какое сообщение приходит, та ли это ошибка...
            actual=false;
        }

        System.out.println("loadFieldsNoTableOverwrite:"+(!actual?"OK":"FAILED"));
        assertFalse(actual);
    }

    // Проверим, поведение, если таблица БД существует, и параметр TableOverwrite=true
    //Должны получить пересозданную таблицу
    @Test
     void loadFieldsTableOverwrite() {
        System.out.println("Проверка: параметр, TableOverwrite=true:");
        boolean actual;
        //initFieldsSet();
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            TableLib.dropTableIfExists(TABLENAME);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            headerCaller.createDBTable(TABLENAME);
            headerCaller.loadFieldsTableOverwrite();

            actual= h.checkTableCreate(TABLENAME)
                    && h.checkTableRecordCreated(TABLENAME)
                    && h.checkAliasesCount(TABLENAME)
                    && h.checkTableFieldsCount(TABLENAME)
                    && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        System.out.println("loadFieldsTableOverwrite:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }

    // Проверим, поведение, если ImportTable=true
    // Должны получить таблицу импорта
    @Test
     void loadFieldsImportTable() {
        System.out.println("Проверка: параметр, ImportTable=true:");
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            TableLib.dropTableIfExists(TABLENAME);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            headerCaller.loadFieldsStandartCall();
            final String tableName=TABLENAME+"_import";
            headerCaller.loadFieldsImportTable();

            actual=h. checkTableCreate(tableName)
                    && !h.checkTableRecordCreated(tableName)
                    && !h.checkAliasesCount(tableName)
                    && h.checkTableFieldsCount(tableName)
                    && !h.checkAliases(tableName);
            TableLib.dropTableIfExists(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME+"_import");
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        System.out.println("loadFieldsImportTable:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }

    // Проверим, поведение, если ImportTable=true и набор полей не совпадеют с основной таблицей
    // Должны получить исключение с сообщением о несоответствии типов полей
    @Test
    void loadFieldsImportTableIllegalAliases() {
        System.out.println("Проверка: параметр, ImportTable=true&Несоответствие наборов псевдонимов:");
        boolean actual;
        HeaderTestHelper h=new HeaderTestHelper();
        try {
            TableLib.dropTableIfExists(TABLENAME);
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            headerCaller.loadFieldsStandartCall();
            headerCaller.changeDBFieldType();
            headerCaller.loadFieldsImportTable();
            actual=false;
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=true;
            TableLib.dropTableIfExists(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME+"_import");
        }
        System.out.println("loadFieldsImportTableIllegalAliases:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }

    //Проверяем работу импорта предопределенных полей.
    //Expect: Поле potrebnost_pen должно быть STRINGTYPE.
    @Test
    void loadFieldsDefaults() {
        boolean actual;
        try {
            HeaderTestHelper h=new HeaderTestHelper();
            HeaderCaller headerCaller=new HeaderCaller(TABLENAME,h.getFIELDS());
            TableLib.dropTableIfExists(TABLENAME);
            headerCaller.loadFieldsDefaults();
            h.getFIELDSSET().put("potrebnost_pen",FieldType.STRINGTYPE);
            actual= h.checkTableCreate(TABLENAME)
                    && h.checkTableRecordCreated(TABLENAME)
                    && h.checkAliasesCount(TABLENAME)
                    && h.checkTableFieldsCount(TABLENAME)
                    && h.checkAliases(TABLENAME);
            TableLib.dropTableIfExists(TABLENAME);
            h.getFIELDSSET().put("potrebnost_pen",FieldType.INTTYPE);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        FieldsDefaults.getFields().remove("potrebnost_pen");
        System.out.println("loadFieldsDefaults:"+(actual?"OK":"FAILED"));
        assertTrue(actual);
    }


    //TODO Проверки: 1. таблица создана. 2. соответствуют имена полей таблицы; 3. соответствует количество полей таблицы; 5. созданы псевдонимы; 6. создана запись в таблице "tables".
    // Сделать это с разными параметрами
    // Сделать проверку fields.ini файла
    // Проверка работы параметров:
    //- Имя файла несуществует
    //- Таблица существует, но прав на перезапись нет.
    // - Псевдонимы создавать не надо
    // - создавать запись таблицы в таблицу tables не надо
    //
}