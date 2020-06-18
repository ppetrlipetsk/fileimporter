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

    public int getRowCount() {
        return rowCount;
    }

    private boolean isStoreAliases() {
        return storeAliases;
    }


    /**
     *
     * При загрузке таблицы есть следующие ситуации:
     *  1. Таблицы загружаются в чистую БД.
     *  2. При загрузке основной таблицы, другая основная таблица с тем же именем, уже загружена
     *  3. При загрузке таблицы изменений, другая таблица изменений с тем же именем, уже существует в БД.
     *  4. При загрузке таблицы изменений, основной таблицы не существует.
     *
     */
    public void loadFields(String tableName, String fileName, boolean importTable, int fieldsCount) throws OpenXML4JException, SAXException, IOException, SQLException,  ImportTableException {
        FieldsCollection fieldsSource=null;
        FieldsCollection fieldsDestination=new FieldsCollection(16, 0.75f,false);
        if (importTable) { //Если это импорт таблицы изменений, в уже имеющуюся таблицу, с определенным набором полей, то загружаем сущействующий набор полей
            fieldsSource=loadFieldsFromDB(tableName);
            if ((fieldsSource.size()==0)) throw new ImportTableException("Таблица изменений должна загружаться после основной таблицы. Основная таблица не найдена."); //Если не нашли информацию о полях основной таблицы-исключение
        }
        FieldsCallback fcb = new FieldsCallback(fieldsCount);
        ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
        ereader.read();
        ereader.close();
        correctFieldTypes(fieldsDestination);
        if (importTable)
            validateFieldsAndAliases(fieldsSource,fieldsDestination);
        createTable(fieldsDestination);
    }

    // Загружает поля из БД
    private FieldsCollection loadFieldsFromDB(String tableName) throws SQLException {
        long tableId = getTableId(tableName);
        return TableClass.getAliasesForTable(tableId);
    }

    // Проверяет, приемлемость типов полей таблиц.
    // т.е. если поле в основной таблице имеет тип STRINGTYPE, а поле в таблице изменений имеет тип INTPE,
    // то тип INTTYPE может быть успешно импртирован в поле с типом STRINGTYPE.
    // А Поле с типом STRINGTYPE не сможет быть импортировано в поле с типом INTTYPE.Если типы не равны, или такого поля в одной из таблиц нет, то false.
    // Поэтому, надо сравнить типы, и если они приемлемы, то продолжить импорт. Иначе исключение.
    private void validateFieldsAndAliases(FieldsCollection fieldsSource, FieldsCollection destination) throws ImportTableException {
        if ((fieldsSource==null)||(destination==null)){
            Formatter f=new Formatter();
            throw new ImportTableException(f.format("Коллекция псевдонимов полей не существует.").toString());
        }
        Iterator<Map.Entry<String, FieldRecord>> itr1 = destination.getIterator();
        while ((itr1.hasNext())) {
            Map.Entry<String, FieldRecord> entry = itr1.next();
            String key=entry.getKey();
            FieldRecord field=  fieldsSource.get(key);

            if ((field==null)||(!field.typeEquals(entry.getValue()))) {
                Formatter f=new Formatter();
                if (field==null)
                    f.format("Поле %s не сществует в таблице импорта.",key);
                else
                    f.format("Несовпадение типов полей. Поле/тип основной табл./тип измененной таблицы:%s/%s/%s",key,fieldsSource.get(key).getFieldType().toString(),entry.getValue().getFieldType().toString());
                throw new ImportTableException(f.toString());
            }
        }
    }

    // Если тип определить не удалось и тип=null, то тип=STRINGTYPE
    private void correctFieldTypes(FieldsCollection fields) {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getIterator();
        while ((itr1.hasNext())) {
            Map.Entry<String, FieldRecord> entry = itr1.next();
            if (entry.getValue().getFieldType()==null) entry.getValue().setFieldType(FieldType.STRINGTYPE);
        }
    }

    private void createTable(FieldsCollection fields) throws SQLException {
        try {
            boolean tableExists = isTableExists(dbTableName);

            if (!(tableExists && !this.tableDropNonPrompt)) {

                if (this.createDBTable || !tableExists) {

                    TableClass.deleteAliases(dbTableName);
                    TableClass.deleteTableAlias(dbTableName);

                    if (tableExists) TableClass.dropTable(dbTableName);
                    tableslib.TableClass.createTable(fields.getFields(), dbTableName);
                    if (!this.importTable) tableslib.TableClass.insertDeletedField(dbTableName);

                } else
                    TableClass.deleteFromTable(dbTableName);

                if (this.storeAliases) {
                    long tableId = getTableId(tableName);
                    tableslib.TableClass.insertAliases(fields.getFields(), "aliases", tableId);
                }
            }
            else throw new RuntimeException("Таблица "+dbTableName+" уже существует. Недостаточно прав на ее уничтожение и создание новой.");

        } catch(SQLException | ConnectException|FieldTypeError e){
            e.printStackTrace();
            Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка создания таблицы " + e.getMessage(), true);
            throw new SQLException(e.getMessage());
        }
    }

    public void loadRecordsToDataBase() {
        //DataImportCallBack fcb = new DataImportCallBack(fieldsCount);
        DataImportCallBack fcb = new DataImportCallBack();
        ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
        try {
            ereader.read();
        } catch (IOException | SAXException | OpenXML4JException e) {
            e.printStackTrace();
        }
        ereader.close();
    }

    private String getFieldNameByStr(String cellname, FieldsCollection fields) {
        String alias= Transliterate.toTransliterate(cellname.toLowerCase());
        return getFieldNameByAlias(alias, fields);
    }

//    private FieldType getFieldTypeByStr(String s){
//        return DetectType.getFieldType(s);
//    }

    private String getFieldNameByAlias(String alias, FieldsCollection fields) {
        if (alias!=null){
            if (alias.length()==0) alias="field";
            if (fields.containsKey(alias)) alias= generateUniqueField(alias,fields);
        }
        return alias;
    }

    // Генерирует уникальное имя поля.
    private String generateUniqueField(String alias,FieldsCollection fields) {
        int fieldIndex=0;
        StringBuilder s=new StringBuilder(alias);
        boolean b=true;
        while(b) {
            //String indxStr= Integer.toString(fieldIndex);
            String indxStr= String.valueOf(fieldIndex);
            s.append(indxStr);
            if (fields.containsKey(s.toString())) {
                s.delete(alias.length(),s.length());
                fieldIndex++;
            }
            else b=false;
        }
        return s.toString();
    }

    private void detectFieldsTypes(LinkedList<String> list,FieldsCollection fields) throws FieldTypeCorrectionError {
        correctLineRecordsType(list,fields);
    }

    private void createFieldsNameMap(LinkedList<String> row, int fieldscount, FieldsCollection fields) {
        for (int i=0;i<fieldscount;i++){
            String cellName=row.get(i);
            String fieldName=getFieldNameByStr(cellName, fields);
            FieldRecord fieldRecord=new FieldRecord(cellName,fieldName,null,null);
            fields.put(fieldName,fieldRecord);
        }
    }

    private  void correctLineRecordsType(LinkedList<String> line, FieldsCollection fields) throws FieldTypeCorrectionError {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getIterator();
        try {
            int i=0;
            int fieldsCount=line.size();

            while ((itr1.hasNext()) && (i < (fieldsCount))) {
                String value = line.get(i);
                Map.Entry<String, FieldRecord> entry = itr1.next();

                boolean b= FieldsDefaults.isFieldExists(this.tableName,entry.getValue().getAlias());//.fields.containsKey(entry.getValue().getAlias());
                if (!b){
                    FieldType currentFieldType = entry.getValue().getFieldType();
                    FieldType newFieldType = DetectType.getFieldType(value);

                    int ct = FieldRecord.compareTypesEnhanced(currentFieldType, newFieldType);
                    if ((ct != 0) && (ct != 1) && (value.length() > 0)) {
                        setCorrectionFieldType(fields.getFields(), entry.getValue().getAlias(), FieldRecord.getTypeByPriority(currentFieldType, newFieldType));
                    }
                }
                else
                    setCorrectionFieldType(fields.getFields(), entry.getValue().getAlias(), FieldsDefaults.getFieldType(this.tableName, entry.getValue().getAlias()));

                i++;
            }
        }
        catch (Exception e){
            throw new FieldTypeCorrectionError("Ошибка изменения типа записи!");
        }
    }

    private  void setCorrectionFieldType(LinkedHashMap<String,FieldRecord> fields, String fieldName, FieldType fieldType) {
        FieldRecord field=fields.get(fieldName);
        field.setFieldType(fieldType);
        fields.put(fieldName,field);
    }

    private void lineProcessor(LinkedList<String> list, long currentRow, int fieldsCount, FieldsCollection fields) {
        if (currentRow==0) createFieldsNameMap(list,fieldsCount,fields);
        else{
            try {
                detectFieldsTypes(list,fields);
            } catch (FieldTypeCorrectionError fieldTypeCorrectionError) {
                fieldTypeCorrectionError.printStackTrace();
            }
        }
    }

    private boolean isTableExists(String dbTableName) {
        try {
            return TableClass.isTableExist(dbTableName);
        } catch (SQLException|ConnectException e) {
            e.printStackTrace();
        }
        return false;
    }

    private long getTableId(String tableName) throws SQLException {
        long  tableId=tableslib.TableClass.getTableId(tableName);
        if ((tableId==-1)&&(isStoreAliases()))
            tableId=tableslib.TableClass.insertTable(tableName);
        return tableId;
    }

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

    private class FieldsCallback implements IParserCallBack {
        private long currentRow;
        private int fieldsCount;

        private FieldsCallback(int fieldscount) {
            this.currentRow = 0;
            this.fieldsCount=fieldscount;
        }

        @Override
        public void call(LinkedList<String> list, FieldsCollection fields) {
            if (currentRow>0)
                System.out.println("Detect fields type. Row #"+currentRow);

            lineProcessor(list, currentRow,fieldsCount,fields);
            currentRow++;
        }
    }

    private class DataImportCallBack implements IParserCallBack {
        private long currentRow;
        //private int fieldsCount;

        @Override
        public void call(LinkedList<String> list, FieldsCollection fields) {
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

    private DataImportCallBack() {
        this.currentRow=-1;
    }

}

    // for tests

    public boolean validateFieldsAndAliases_Check(FieldsCollection fieldsSource, FieldsCollection destination) throws ImportTableException {
        validateFieldsAndAliases( fieldsSource,  destination);
        return true;
    }
}

