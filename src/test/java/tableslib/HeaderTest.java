package tableslib;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.programparameters.ProgramParameters;
import environment.ApplicationGlobals;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    void loadFields() {
        try {
            dataBaseConnection();

            final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
            final String tableName = "example1";
            final int fieldsCount = 8;
            final boolean storeAliases = true;
            final boolean createTable = true;
            final boolean importTable = false;
            final boolean tableDropNonPrompt = false;
            loadfields1(tableName,fileName,importTable,tableDropNonPrompt,createTable,storeAliases,fieldsCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
        boolean actual=checkLoadFields1();
        assertEquals(actual,false);
        System.out.println("LoadFields1: OK");
    }

    private boolean checkLoadFields1() {
        return false;
    }

    private void loadfields1(String tableName, String fileName, boolean importTable, boolean tableDropNonPrompt, boolean createTable, boolean storeAliases, int fieldsCount) throws SQLException, OpenXML4JException, ImportTableException, SAXException, IOException {
        Header h=new Header();
        h.loadFields(tableName,fileName,importTable,tableDropNonPrompt,createTable,storeAliases,fieldsCount);
    }

    //TODO Проверки: 1. таблица создана. 2. соответствуют имена полей; 3. соответствует количество полей; 5. созданы псевдонимы; 6. создана запись в таблице "tables".
    // Сделать это с разными параметрами
}