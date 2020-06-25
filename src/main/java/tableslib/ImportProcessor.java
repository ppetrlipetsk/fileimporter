package tableslib;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.*;
import environment.QueryRepository;
import excelengine.ExcelReader;
import excelengine.IParserCallBack;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;
import throwlib.FieldTypeError;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class ImportProcessor {
    private int rowCount;
    //private String dbTableName; //имя таблицы БД, в которую будут записываться строки данных. Строится на основе tableName и если importTable=true, то добавляется суффикс "_import"

    public ImportProcessor() {
        this.rowCount=0;
    }

    public void loadRecordsToDataBase(FieldsCollection fields,String fileName,String dbTableName) {
        DataImportCallBack fcb = new DataImportCallBack(fields,dbTableName);
        ExcelReader ereader = new ExcelReader(fileName, fcb, fields.size());
        try {
            ereader.read();
        } catch (IOException | SAXException | OpenXML4JException e) {
            e.printStackTrace();
        }
        ereader.close();
    }

    private void lineImporter(LinkedList<String> list, long currentRow, FieldsCollection fields, String dbTableName) {
        if (currentRow>0){
            importRow(list,fields, dbTableName);
        }
    }

    private void importRow(LinkedList<String> list, FieldsCollection fields, String dbTableName) {
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
        String dbTableName;

        @Override
        public void call(LinkedList<String> list) {
            ++currentRow;

            if (currentRow>0)
            System.out.println("Import row №"+currentRow);

            lineImporter(list, currentRow, fields,dbTableName);

            if (currentRow>0)
                rowCount++;
        }

    private DataImportCallBack(FieldsCollection fields, String dbTableName) {
        this.currentRow=-1;
        this.fields=fields;
        this.dbTableName=dbTableName;
    }

}

}

