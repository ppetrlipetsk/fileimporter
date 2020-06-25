import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsCollection;
import environment.ApplicationGlobals;
import org.junit.jupiter.api.Test;
import tableslib.ImportProcessor;
import tableslib.TableLib;
import tableslib.header.HeaderTestHelper;
import tableslib.importprocessor.ImportProcessotTestHelper;

import java.sql.SQLException;
import static org.junit.jupiter.api.Assertions.*;

class ImportProcessorTest {
    private static final String TABLENAME="example1";
    private static final String FILENAME="C:\\files\\tmc\\xls\\example1.xlsx";
    static {
        try {
            dataBaseConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static void dataBaseConnection() throws Exception {
        String instanceName = "localhost\\MSSQLSERVER";
        String databaseName = "dogc";
        String userName = "sa";
        String password = "win";
        String connectionUrl = "jdbc:sqlserver://%1$s;databaseName=%2$s;integratedSecurity=true";

        try {
            DataBaseConnector.connectDataBase(connectionUrl,userName,password,instanceName,databaseName);
        } catch (SQLException |ClassNotFoundException e) {
            throw new Exception("Ошибка подключения к БД..."+ ApplicationGlobals.getDBInstanceName()+" Сообщение об ошибке:"+e.toString());
        }
    }

    @Test
    void importRecordsToDataBase() {
        TableLib.dropTableIfExists(TABLENAME);
        HeaderTestHelper h=new HeaderTestHelper();
        tableslib.header.HeaderCaller caller=new tableslib.header.HeaderCaller(TABLENAME,h.getFIELDS());
        FieldsCollection fields=null;
        boolean actual=false;
        try {
            fields= caller.loadFieldsStandartCall();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (fields!=null) {
            ImportProcessor ip = new ImportProcessor();
            ip.loadRecordsToDataBase(fields, FILENAME, TABLENAME);
            actual= ImportProcessotTestHelper.checkImportetData(TABLENAME,fields);
        }
        TableLib.dropTableIfExists(TABLENAME);
        assertTrue(actual);
    }
}