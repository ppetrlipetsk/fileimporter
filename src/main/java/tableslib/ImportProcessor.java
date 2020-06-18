package tableslib;

import com.ppsdevelopment.converters.Transliterate;
import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.*;
import environment.QueryRepository;
import excelengine.ExcelReader;
import excelengine.IParserCallBack;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;
import throwlib.FieldTypeCorrectionError;
import throwlib.FieldTypeError;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class ImportProcessor {
    //private FieldsCollection fields=new FieldsCollection(16, 0.75f,false);

    private final boolean storeAliases;
    private final boolean createDBTable;
    private final String fileName;
    private final String tableName;
    private final int fieldsCount;
    private int rowCount;
    private boolean importTable;
    private String dbTableName; //имя таблицы БД, в которую будут записываться строки данных. Строится на основе tableName и если importTable=true, то добавляется суффикс "_import"
    private boolean tableDropNonPrompt;


    public ImportProcessor(String fileName, String tableName, int fieldsCount,   boolean storeAliases, boolean createDBTable, boolean importTable, boolean tableDropNonPrompt) {
        this.storeAliases = storeAliases;
        this.createDBTable = createDBTable;
        this.fileName=fileName;
        this.tableName=tableName;
        this.fieldsCount=fieldsCount;
        this.rowCount=0;
        this.importTable=importTable;
        this.tableDropNonPrompt=tableDropNonPrompt;
        dbTableName= this.importTable ? tableName+"_import" : tableName;
    }

    public void loadRecordsToDataBase(FieldsCollection fields) {
        //DataImportCallBack fcb = new DataImportCallBack(fieldsCount);
        DataImportCallBack fcb = new DataImportCallBack(fields);
        ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
        try {
            ereader.read();
        } catch (IOException | SAXException | OpenXML4JException e) {
            e.printStackTrace();
        }
        ereader.close();
    }



//    private FieldType getFieldTypeByStr(String s){
//        return DetectType.getFieldType(s);
//    }




    private void lineImporter(LinkedList<String> list, long currentRow, FieldsCollection fields) {
        if (currentRow>0){
            importRow(list,fields);
        }
    }

    private void importRow(LinkedList<String> list, FieldsCollection fields) {
        try {
            insertRecord(fields, dbTableName, list);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void insertRecord(FieldsCollection fields, String tableName, LinkedList<String> list) throws SQLException {

        String query= QueryRepository.getRecordInsertQuery().replace("@tablename@",tableName);
        RecordInsertQueryFiller filler=new RecordInsertQueryFiller(fields, tableName, list);
        query=query.replace("@fields@",filler.getFieldsNamesQueryString());

        try {
            DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
            query=query.replace("@values@",filler.getValuesStr());
            dp.exec(query);
        } catch (SQLException|FieldTypeError|ConnectException|ParseException e) {
          //  e.printStackTrace();
            throw new SQLException(e);
        }

    }


    private class DataImportCallBack implements IParserCallBack {
        private long currentRow;
        //private int fieldsCount;
        FieldsCollection fields;

        @Override
        public void call(LinkedList<String> list) {
            ++currentRow;

            if (currentRow>0)
            System.out.println("Import row №"+currentRow);

            lineImporter(list, currentRow, fields);

            if (currentRow>0)
                rowCount++;
        }

//        private DataImportCallBack(int fieldsCount) {
////            this.fieldsCount=fieldsCount;
//            this.currentRow=-1;
//        }
//
//    }

    private DataImportCallBack(FieldsCollection fields) {
        this.currentRow=-1;
        this.fields=fields;
    }

}

    // for tests


}

