package tableslib.header;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldType;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;

public class HeaderTestHelper {
    private final static String[] FIELDS ={"material","kratkii_tekst_materiala","potrebnost_pen","pozitsiya_potrebnosti_pen","obem_potrebnosti","data_poslednego_izmeneniya_pozitsii_potr_","pozitsiya_potrebnosti_pen0","kratkii_tekst_materiala0"};
    private static HashMap<String, FieldType> FIELDSSET=new HashMap<>();

    public HeaderTestHelper() {
        initFieldsSet();
    }

    public String[] getFIELDS() {
        return FIELDS;
    }

    public HashMap<String, FieldType> getFIELDSSET() {
        return FIELDSSET;
    }

    public void initFieldsSet() {
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


    public void dropTableIfExists(String tableName){
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


    // Проверяем, создана ли таблица в БД
    public boolean checkTableCreate(String tableName) {
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
    public boolean checkTableRecordCreated(String tableName) {
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
    public boolean checkAliases(String tableName) {
        System.out.print("Проверка псевдонимов:");
        HashMap<String, FieldType> fields=new HashMap<>();
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
        System.out.println((result?"OK":"FAILED"));

        return result;
    }

    private boolean checkAliasFields(HashMap<String, FieldType> fields) {
        return (FIELDSSET.size()==fields.size())&&(fields.entrySet().containsAll(FIELDSSET.entrySet()))&&(FIELDSSET.entrySet().containsAll(fields.entrySet()));
    }


    private String serializeFields(){
        String s = "'" + Arrays.toString(FIELDS) +"'";
        return s.replace(",","','").replace("]","").replace("[","").replace(" ","");
    }

    int getDBAliasesCount(String tableName){
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
    boolean checkAliasesCount(String tableName) {
        boolean result=getDBAliasesCount(tableName)==FIELDS.length;
        System.out.print("Проверка количества полей псевдонимов:");
        String s=result?"OK":"FAILED";
        System.out.println(s);
        return result;
    }

    // Проверяем соответствие количества и имен полей в созданной таблице БД, количеству и именам полей в эталонной коллекции.
    boolean checkTableFieldsCount(String tableName) {
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
            //if (deletedField)count--;
            actual=count==FIELDS.length;
            if (actual) System.out.println("OK");
        } catch (SQLException e) {
            System.out.println("FAILED");
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        if (!actual) System.out.println("Набор полей в созданной таблице не соответствует");

        return actual;
    }


}
