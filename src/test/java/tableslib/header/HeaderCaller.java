package tableslib.header;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsCollection;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsDefaults;
import tableslib.Header;

import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;

public class HeaderCaller {
    private  String[] fields;
    private final String TABLENAME;

    public HeaderCaller(String TABLENAME, String[] fields) {
        this.TABLENAME = TABLENAME;
        this.fields =fields;
    }

    public FieldsCollection loadFieldsStandartCall() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = true;
        return execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsIllegalFileName() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example12.xlsx";
        final String tableName = "example1";
        final int fieldsCount = fields.length;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = false;
        execLoadFields(tableName,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsOverFieldsCount() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length+2;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = false;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsLessFieldCount() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length-1;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = false;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsNoStoreAliases() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length;
        final boolean storeAliases = false;
        final boolean importTable = false;
        final boolean tableOverwrite = false;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsNoTableOverwrite(int fieldsCount) throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final boolean storeAliases = false;
        final boolean importTable = false;
        final boolean tableOverwrite = false;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsTableOverwrite() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = true;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }


    private FieldsCollection execLoadFields(String tableName, String fileName, boolean importTable, boolean tableOverwrite, boolean storeAliases, int fieldsCount) throws Exception {
        Header h=new Header();
        return h.loadFields(tableName,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void loadFieldsImportTable() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final boolean storeAliases = false;
        final boolean importTable = true;
        final boolean tableOverwrite = true;
        final int fieldsCount = fields.length;
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

//    public void loadFieldsImportTableIllegalAliases() throws Exception {
//        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
//        final boolean storeAliases = false;
//        final boolean importTable = true;
//        final boolean tableOverwrite = true;
//        final int fieldsCount = fields.length;
//        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
//    }


    public void loadFieldsDefaults() throws Exception {
        final String fileName = "C:\\files\\tmc\\xls\\example1.xlsx";
        final int fieldsCount = fields.length;
        final boolean storeAliases = true;
        final boolean importTable = false;
        final boolean tableOverwrite = true;
        loadFieldsDefaultTypes();
        execLoadFields(TABLENAME,fileName,importTable,tableOverwrite,storeAliases,fieldsCount);
    }

    public void createDBTable(String tableName) {
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
        } catch (SQLException | ConnectException e) {
            e.printStackTrace();
        }
    }

    public void changeDBFieldType() {
        String query="UPDATE [dbo].[aliases]\n" +
                "   SET [fieldtype] = 'STRINGTYPE'\n" +
                " WHERE fieldalias='potrebnost_pen' and table_id in (select id from tables where tablename='example1')\n";
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection())) {
            dp.exec(query);
        } catch (SQLException | ConnectException e) {
            e.printStackTrace();
        }
    }

    private static void  loadFieldsDefaultTypes() throws IOException {
            String fileName = "example1.ini";
            String tableName = "example1";
            try {
                FieldsDefaults.loadDefaultFields(fileName, tableName);
            } catch (IOException e) {
                throw new IOException("Ошибка загрузки информации о предопределенных типах полей. Сообщение об ошибке:"+e.toString());
            }
    }

}
