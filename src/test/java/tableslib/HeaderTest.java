package tableslib;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldType;
import environment.ApplicationGlobals;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class HeaderTest {

    private static String[] FIELDS ={"material","kratkii_tekst_materiala","potrebnost_pen","pozitsiya_potrebnosti_pen","obem_potrebnosti","data_poslednego_izmeneniya_pozitsii_potr_","pozitsiya_potrebnosti_pen0","kratkii_tekst_materiala0"};
    private static HashMap<String, FieldType> FIELDSSET=new HashMap<>();
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

        initFieldsSet();
    }

    private static void initFieldsSet() {
        FIELDSSET.clear();
        FIELDSSET.put("material", FieldType.STRINGTYPE);
        FIELDSSET.put("kratkii_tekst_materiala",FieldType.STRINGTYPE);
        FIELDSSET.put("potrebnost_pen",FieldType.BIGINTTYPE);
        FIELDSSET.put("pozitsiya_potrebnosti_pen",FieldType.INTTYPE);
        FIELDSSET.put("obem_potrebnosti",FieldType.FLOATTYPE);
        FIELDSSET.put("data_poslednego_izmeneniya_pozitsii_potr_",FieldType.DATETYPE);
        FIELDSSET.put("pozitsiya_potrebnosti_pen0",FieldType.STRINGTYPE);
        FIELDSSET.put("kratkii_tekst_materiala0",FieldType.LONGSTRINGTYPE);
    }

    // Проверяем выполнение:
    // - создание таблицы
    // - создание записи таблицы в таблице tables
    // - создание псевдонимов (набор полей, их имена, количество, соответствие типов заданномй коллекции)
    //- соответствие имен полей таблицы БД, заданному набору эталонной коллекции.
    @Test
    void loadFields() {
        boolean actual;
        try {
            //dropTableIfExists(TABLENAME);
            initFieldsSet();
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = true;
            final boolean importTable = false;
            final boolean tableOverwrite = true;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
             actual= checkTableCreate(TABLENAME)
                    && checkTableRecordCreated(TABLENAME)
                    && checkAliasesCount(TABLENAME)
                    && checkTableFieldsCount(TABLENAME,importTable)
                    && checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        String s=actual?"OK":"FAILED";
        System.out.println("LoadFields1:"+s);
        assertTrue(actual);
    }

    // Задаем неверное имя файла
    @Test
    void loadFieldsBadFileName() {
        // Задаем неправильное имя файла. Должно быть исключение и выход.
        boolean result;
        try {
            final String fileName = "C:\\files\\tmc\\xls\\example12.xlsx";
            final String tableName = "example1";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = true;
            final boolean createTable = true;
            final boolean importTable = false;
            final boolean tableOverwrite = false;
            execLoadFields(tableName,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            result = checkTableCreate(TABLENAME)
                        && checkTableRecordCreated(TABLENAME)
                        && checkAliasesCount(TABLENAME)
                        && checkTableFieldsCount(TABLENAME,importTable)
                        && checkAliases(TABLENAME);

        } catch (Exception e) {
            System.out.println(e.toString());
            result=false;
        }
        String s=result?"OK":"FAILED";
        System.out.println("loadFieldsBadFileName:"+s);

        assertFalse(result);

    }

    // Неверное количество полей. Больше, чем нужно. В этом случае должны создаться дополнительные поля с именами fields...
    @Test
    void loadFieldsIllegalFieldCount() {
        initFieldsSet();
        FIELDSSET.put("field",FieldType.STRINGTYPE);
        FIELDSSET.put("field0",FieldType.STRINGTYPE);
        boolean actual;
        try {
            dropTableIfExists(TABLENAME);
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length+2;
            final boolean storeAliases = true;
            final boolean createTable = true;
            final boolean importTable = false;
            final boolean tableOverwrite = false;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= checkTableCreate(TABLENAME)
                    && checkTableRecordCreated(TABLENAME)
                    && checkAliasesCount(TABLENAME)
                    && checkTableFieldsCount(TABLENAME,importTable)
                    && checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        String s=actual?"OK":"FAILED";
        System.out.println("LoadFieldsIllegalFieldsCount:"+s);
        assertTrue(actual);
    }

    // Задаем меньшее количество полей, меньше, чем есть в таблице XSLX
    @Test
    public void loadFieldsIllegalFieldCountLessThanNeed() {
        System.out.println("Проверка: параметр, задающий количество полей в таблице XLSX, задан меньше, чем в эталонной коллекции");
        initFieldsSet();
        boolean actual;
        FIELDSSET.put("field",FieldType.STRINGTYPE);
        FIELDSSET.put("field0",FieldType.STRINGTYPE);
        try {
            dropTableIfExists(TABLENAME);
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length-1;
            final boolean storeAliases = true;
            final boolean importTable = false;
            final boolean tableOverwrite = false;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= checkTableCreate(TABLENAME)
                    && checkTableRecordCreated(TABLENAME)
                    && !checkAliasesCount(TABLENAME)
                    && !checkTableFieldsCount(TABLENAME,importTable)
                    && checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        String s=!actual?"OK":"FAILED";
        System.out.println("loadFieldsIllegalFieldCountLessThanNeed:"+s);
        assertFalse(actual);
    }

    // Создаем таблицу, не сохраняем псевдонимы
    @Test
    public void loadFieldsNoStoreAliases() {
        System.out.println("Проверка: параметр, storeAliases=false");
        initFieldsSet();
        boolean actual;
        try {
            dropTableIfExists(TABLENAME);
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = false;
            final boolean importTable = false;
            final boolean tableOverwrite = false;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= checkTableCreate(TABLENAME)
                    && !checkTableRecordCreated(TABLENAME)
                    && getDBAliasesCount(TABLENAME)==0
                    && checkTableFieldsCount(TABLENAME,importTable)
                    && !checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        if (actual) System.out.println("Количество записей в таблице псевдонимов=0");
        String s=actual?"OK":"FAILED";
        System.out.println("loadFieldsNoStoreAliases:"+s);
        assertTrue(actual);
    }


    // Проверим, поведение, если таблица БД существует, а параметр TableOverwrite=false
    //Должны получить сообщение о нехватке прав на удаление существующей таблицы.
    @Test
    public void loadFieldsNoTableOverwrite() {
        System.out.println("Проверка: параметр, TableOverwrite=false:");
        boolean actual;
        initFieldsSet();
        try {
            dropTableIfExists(TABLENAME);
            createDBTable(TABLENAME);
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = false;
            final boolean importTable = false;
            final boolean tableOverwrite = false;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= !checkTableCreate(TABLENAME)
                    && !checkTableRecordCreated(TABLENAME)
                    && getDBAliasesCount(TABLENAME)==0
                    && !checkTableFieldsCount(TABLENAME,importTable)
                    && !checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        //if (actual) System.out.println("Количество записей в таблице псевдонимов=0");
        String s=!actual?"OK":"FAILED";
        System.out.println("loadFieldsNoTableOverwrite:"+s);
        assertFalse(actual);
    }

    // Проверим, поведение, если таблица БД существует, и параметр TableOverwrite=true
    //Должны получить пересозданную таблицу
    @Test
    public void loadFieldsTableOverwrite() {
        System.out.println("Проверка: параметр, TableOverwrite=true:");
        boolean actual;
        initFieldsSet();
        try {
            dropTableIfExists(TABLENAME);
            createDBTable(TABLENAME);
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = true;
            final boolean importTable = false;
            final boolean tableOverwrite = true;
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= checkTableCreate(TABLENAME)
                    && checkTableRecordCreated(TABLENAME)
                    && checkAliasesCount(TABLENAME)
                    && checkTableFieldsCount(TABLENAME,importTable)
                    && checkAliases(TABLENAME);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        String s=actual?"OK":"FAILED";
        System.out.println("loadFieldsTableOverwrite:"+s);
        assertTrue(actual);
    }

    // Проверим, поведение, если ImportTable=true
    //Должны получить пересозданную таблицу
    @Test
    public void loadFieldsImportTable() {
        System.out.println("Проверка: параметр, ImportTable=true:");
        boolean actual;
        initFieldsSet();
        try {
        //    dropTableIfExists();
//            createDBTable();
            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final int fieldsCount = FIELDS.length;
            final boolean storeAliases = true;
            final boolean importTable = true;
            final boolean tableOverwrite = true;
            final String tableName=TABLENAME+"_import";
            execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
            actual= checkTableCreate(tableName)
                    && !checkTableRecordCreated(tableName)
                    && !checkAliasesCount(tableName)
                    && checkTableFieldsCount(tableName,!importTable)
                    && !checkAliases(tableName);
        } catch (Exception e) {
            System.out.println(e.toString());
            actual=false;
        }
        String s=actual?"OK":"FAILED";
        System.out.println("loadFieldsImportTable:"+s);
        assertTrue(actual);
    }



    private void createDBTable(String tableName) {
        String query="CREATE TABLE [dbo].["+tableName+"](\n" +
                "\t[id] [int] IDENTITY(1,1) NOT NULL,\n" +
                "\t[material] [varchar](1000) NULL,\n" +
                "\t[kratkii_tekst_materiala] [varchar](1000) NULL,\n" +
                "\t[potrebnost_pen] [bigint] NULL,\n" +
                "\t[pozitsiya_potrebnosti_pen] [int] NULL,\n" +
                "\t[obem_potrebnosti] [float] NULL,\n" +
                "\t[data_poslednego_izmeneniya_pozitsii_potr_] [date] NULL,\n" +
                "\t[pozitsiya_potrebnosti_pen0] [varchar](1000) NULL,\n" +
                "\t[kratkii_tekst_materiala0] [varchar](5000) NULL,\n" +
                "\t[deleted] [int] NULL\n" +
                ") ON [PRIMARY]";
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            //ResultSet resultSet = dp.query(query);
            dp.exec(query);
        } catch (SQLException|ConnectException e) {
            e.printStackTrace();
        }
    }


    private void dropTableIfExists(String tableName){
        String query="delete from aliases where table_id in (select id from tables where tablename='"+tableName+"')\n" +
                "delete from tables where tablename='"+tableName+"'" +
                "drop table "+tableName;
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            //ResultSet resultSet = dp.query(query);
            dp.exec(query);
        } catch (SQLException|ConnectException e) {
            e.printStackTrace();
        }

    }

//    private boolean checkLoadFields() {
//        //assertEquals(actual,true);
//        return checkTableCreate()
//                && checkTableRecordCreated()
//                && checkAliasesCount()
//                && checkTableFieldsCount()
//                && checkAliases();
//    }

    // Проверяем, создана ли таблица в БД
    private boolean checkTableCreate(String tableName) {
        boolean actual=false;
        String query="IF EXISTS (SELECT 1 \n" +
                "           FROM INFORMATION_SCHEMA.TABLES \n" +
                "           WHERE TABLE_TYPE='BASE TABLE' \n" +
                "           AND TABLE_NAME='"+tableName+"') \n" +
                "   SELECT 1 AS res ELSE SELECT 0 AS res;";
        //DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())){
            ResultSet resultSet=dp.query(query);
            if ((resultSet != null)) {
                while (resultSet.next()) {
                    int exists = resultSet.getInt("res");
                    if (exists==1)
                        System.out.println("Таблица создана");
                    else
                    {
                        System.out.println("Таблица не создана");
                        actual=false;
                    }
                }
            }
            actual=true;
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        return actual;
    }

    // Проверяем, создана ли таблица в таблице tables
    private boolean checkTableRecordCreated(String tableName) {
        boolean actual=false;
        String query="IF EXISTS (SELECT 1  FROM tables WHERE tablename='"+tableName+"') SELECT 1 AS res ELSE SELECT 0 AS res";

        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())){
            ResultSet resultSet=dp.query(query);
            if ((resultSet != null)) {
                while (resultSet.next()) {
                    int exists = resultSet.getInt("res");
                    if (exists==1)
                        System.out.println("В таблице tables создана запись для таблицы examples1");
                    else
                    {
                        System.out.println("В таблице tables не создана запись для таблицы examples1");
                        return false;
                    }
                }
            }
            actual=true;
        } catch (SQLException e) {
            //e.printStackTrace();
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        return actual;
    }

    //Проверяем, создани псевдонимы в тиаблице aliases, и соответствуют ли они эталонной коллекции
    private boolean checkAliases(String tableName) {
        System.out.print("Проверка псевдонимов:");
        HashMap<String,FieldType> fields=new HashMap<>();
        String query="select * from aliases where table_id in (select id from tables where tablename='"+tableName+"')";
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection()))
        {
            ResultSet resultSet=dp.query(query);
            if ((resultSet != null)) {
                while (resultSet.next()) {
                    fields.put(resultSet.getString("fieldalias"),FieldType.valueOf(resultSet.getString("fieldtype")));
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
            return false;
        }
        boolean result=checkAliasFields(fields);
        if (result) System.out.println("OK");
        else
            System.out.println("FAILED");
        return result;
    }

    private boolean checkAliasFields(HashMap<String, FieldType> fields) {
        return (FIELDSSET.size()==fields.size())&&(fields.entrySet().containsAll(FIELDSSET.entrySet()))&&(FIELDSSET.entrySet().containsAll(fields.entrySet()));
    }

//
//    private int getTableId(){
//        String query="select id from tables where tablename='example1'";
//        int id=-1;
//        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());){
//            id = (Integer) dp.query(query, "id");
//        }
//        catch (Exception e){
//        }
//        return id;
//    }

    private String serializeFields(){
        String s = "'" + Arrays.toString(FIELDS) +"'";
        return s.replace(",","','").replace("]","").replace("[","").replace(" ","");
    }

    private int getDBAliasesCount(String tableName){
        int count=-1;
        String query="SELECT count(id) as countid  FROM aliases WHERE table_id in (select id from tables where tablename='"+tableName+"')"+
                "and fieldalias in ("+serializeFields()+")";
        try(DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            count= (int)dp.query(query,"countid");
        } catch (SQLException e) {
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        return count;
    }

    // Проверяем количество записей в таблице псевдонимов
    private boolean checkAliasesCount(String tableName) {
        boolean result=getDBAliasesCount(tableName)==FIELDS.length;
        System.out.print("Проверка количества полей псевдонимов:");
        String s=result?"OK":"FAILED";
        System.out.println(s);
        return result;
    }

    // Проверяем соответствие количества и имен полей в созданной таблице БД, количеству и именам полей в эталонной коллекции.
    private boolean checkTableFieldsCount(String tableName,boolean deletedField) {
        boolean actual=false;
        System.out.print("Проверка, соответствует ли количество полей в созданной таблице:");
        String query="select count(name) as countid from (\n" +
                "SELECT  c.name\n" +
                "FROM    syscolumns c\n" +
                "    INNER JOIN systypes t ON c.xtype = t.xtype and c.usertype = t.usertype\n" +
                "WHERE   c.id = OBJECT_ID('"+tableName+"')) i1 where\n" +
                "i1.name in ("+serializeFields()+")";
        int count;
        try(DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            count= (int)dp.query(query,"countid");
            if (deletedField)count--;
            actual=count==FIELDS.length;
            if (actual) System.out.println("OK");
        } catch (SQLException e) {
            System.out.println("FAILED");
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        if (!actual) System.out.println("Набор полей в созданной таблице не соответствует");

        return actual;
    }


//    // Проверяем, соответствует ли набор псевдонимов
//    private boolean checkLoadFields6() {
//        return false;
//    }
//
//    // Проверяем, создастся ли таблица, если параметр prompt=true
//    private boolean checkLoadFields7() {
//        return false;
//    }


    private void execLoadFields(String tableName, String fileName, boolean importTable, boolean tableOverwrite, boolean storeAliases, int fieldsCount) throws Exception {
        Header h=new Header();
//        try {
            h.loadFields(tableName,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
//        } catch (Exception e) {
// //           e.printStackTrace();
//            System.out.println(e.toString());
//        }
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