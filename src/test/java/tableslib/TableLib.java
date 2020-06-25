package tableslib;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;

import java.net.ConnectException;
import java.sql.SQLException;

public class TableLib {
    public static void dropTableIfExists(String tableName){
        String query="delete from aliases where table_id in (select id from tables where tablename='"+tableName+"')\n" +
                "delete from tables where tablename='"+tableName+"'" +
                "drop table "+tableName;
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            dp.exec(query);
        } catch (SQLException | ConnectException e) {
            e.printStackTrace();
        }

    }

}
