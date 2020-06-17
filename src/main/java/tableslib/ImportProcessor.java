package tableslib;

import com.ppsdevelopment.converters.Transliterate;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldRecord;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldType;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldsDefaults;
import excelengine.ExcelReader;
import excelengine.IParserCallBack;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.xml.sax.SAXException;
import throwlib.EIllegalFieldsSet;
import throwlib.FieldTypeCorrectionError;
import throwlib.FieldTypeError;
import java.io.IOException;
import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static environment.QueryRepository.getRecordInsertQuery;

public class ImportProcessor {
    FieldsCollection fields=new FieldsCollection(16, 0.75f,false);

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

    public boolean isStoreAliases() {
        return storeAliases;
    }

    public void loadRecordsToDataBase() {
        DataImportCallBack fcb = new DataImportCallBack(fieldsCount);
        ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
        try {
            ereader.read();
        } catch (IOException | SAXException | OpenXML4JException e) {
            e.printStackTrace();
        }
        ereader.close();
    }

    public void loadFields() throws OpenXML4JException, SAXException, IOException, SQLException, FieldTypeError, EIllegalFieldsSet {
        FieldsCollection fieldsSource=new FieldsCollection(16, 0.75f,false);
        if (this.importTable) {
            loadFieldsFromDB(fieldsSource);
        }

        // Если это не импорт измененной таблицы или если при чтении из таблицы aliases данные не найдены
        if ((!this.importTable) || (fields.getFields().size()==0)){
            FieldsCallback fcb = new FieldsCallback(fieldsCount);
            ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
            ereader.read();
            correctFieldTypes();
            ereader.close();
        }

        if ((this.importTable&&isFieldsEquals(fieldsSource))||(!this.importTable))
            createTable();
        else
            throw new EIllegalFieldsSet("Несовпадение типа полей в таблицах.");
    }

    private void loadFieldsFromDB(FieldsCollection fieldsSource) throws SQLException {
        long tableId = getTableId(tableName);
        ResultSet resultSet = tableslib.TableClass.getAliasesForTable(tableId);
        if ((resultSet != null)) {
            while (resultSet.next()) {
                String fieldalias = resultSet.getString("fieldalias");
                FieldType fieldType = TableClass.detectFieldType(resultSet.getString("fieldtype"));
                fieldsSource.getFields().put(fieldalias, new FieldRecord(fieldalias, fieldalias, null, fieldType));
            }
        }
    }

    private boolean isFieldsEquals(FieldsCollection fieldsSource){
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
        while ((itr1.hasNext())) {
            Map.Entry<String, FieldRecord> entry = itr1.next();
            String key=entry.getKey();
            FieldRecord field=  fieldsSource.getFields().get(key);
            if (field.getFieldType()!=entry.getValue().getFieldType())
                return false;
        }
        return true;
    }

    private void correctFieldTypes() {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
        while ((itr1.hasNext())) {
            Map.Entry<String, FieldRecord> entry = itr1.next();
            if (entry.getValue().getFieldType()==null) entry.getValue().setFieldType(FieldType.STRINGTYPE);
        }
    }

    private String getFieldNameByStr(String cellname) {
        String alias= Transliterate.toTransliterate(cellname);
        String fieldName=getFieldNameByAlias(alias);
        return fieldName;
    }

    private FieldType getFieldTypeByStr(String s){
        return DetectTypeClass.getFieldType(s);
    }

    private String getFieldNameByAlias(String alias) {
        if (alias!=null){
            if (alias.length()==0) alias="field";
            if (fields.getFields().containsKey(alias)) alias= generateUniqueField(alias);
        }
        return alias;
    }

    // Генерирует уникальное имя поля.
    private String generateUniqueField(String alias) {
        int fieldIndex=0;
        StringBuilder s=new StringBuilder(alias);
        boolean b=true;
        while(b) {
            //String indxStr= Integer.toString(fieldIndex);
            String indxStr= String.valueOf(fieldIndex);
            s.append(indxStr);
            if (fields.getFields().containsKey(s.toString())) {
                s.delete(alias.length(),s.length());
                fieldIndex++;
            }
            else b=false;
        }
        return s.toString();
    }

    protected void detectFieldsTypes(LinkedList<String> list) throws FieldTypeCorrectionError {
        correctLineRecordsType(list,fields);
    }

    private void createFieldsNameMap(LinkedList<String> row, int fieldscount) {
        for (int i=0;i<fieldscount;i++){
            String cellName=row.get(i);
            String fieldName=getFieldNameByStr(cellName);
            FieldRecord fieldRecord=new FieldRecord(cellName,fieldName,null,null);
            fields.getFields().put(fieldName,fieldRecord);
        }
    }

    private  void correctLineRecordsType(LinkedList<String> line, FieldsCollection fields) throws FieldTypeCorrectionError {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
        try {
            int i=0;
            int fieldsCount=line.size();

            while ((itr1.hasNext()) && (i < (fieldsCount))) {
                String value = line.get(i);
                Map.Entry<String, FieldRecord> entry = itr1.next();

                boolean b= FieldsDefaults.isFieldExists(this.tableName,entry.getValue().getAlias());//.fields.containsKey(entry.getValue().getAlias());
                if (!b){
                    FieldType currentFieldType = entry.getValue().getFieldType();
                    FieldType newFieldType = DetectTypeClass.getFieldType(value);

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

    private void lineProcessor(LinkedList<String> list, long currentRow, int fieldsCount) {
        if (currentRow==0) createFieldsNameMap(list,fieldsCount);
        else{
            try {
                detectFieldsTypes(list);
            } catch (FieldTypeCorrectionError fieldTypeCorrectionError) {
                fieldTypeCorrectionError.printStackTrace();
            }
        }
    }

    public void createTable() throws SQLException, FieldTypeError {
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

            } catch(SQLException | ConnectException e){
                e.printStackTrace();
                Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка создания таблицы " + e.getMessage(), true);
                throw new SQLException(e.getMessage());
            }
        catch(FieldTypeError fieldTypeError){
                fieldTypeError.printStackTrace();
                Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка создания таблицы " + fieldTypeError.getMessage(), true);
                throw new SQLException(fieldTypeError.getMessage());
            }

    }

    private boolean isTableExists(String dbTableName) {
        try {
            return TableClass.isTableExist(dbTableName);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ConnectException e) {
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

    private void lineImporter(LinkedList<String> list, long currentRow, int fieldsCount) {
        if (currentRow>0){
            importRow(list);
        }
    }

    private void importRow(LinkedList<String> list) {
        try {
            insertRecord(fields, dbTableName, list);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    private void insertRecord(FieldsCollection fields, String tableName, LinkedList<String> list) throws SQLException {

        String query=getRecordInsertQuery().replace("@tablename@",tableName);
        RecordInsertQueryFiller filler=new RecordInsertQueryFiller(fields, tableName, list);
        query=query.replace("@fields@",filler.getFieldsNamesQueryString());

        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            query=query.replace("@values@",filler.getValuesStr());
            dp.exec(query);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        } catch (ParseException e) {
            e.printStackTrace();
        } catch (FieldTypeError fieldTypeError) {
            fieldTypeError.printStackTrace();
        } catch (ConnectException e) {
            e.printStackTrace();
        }

    }

    private class FieldsCallback implements IParserCallBack {
        private long currentRow;
        private int fieldsCount;

        public FieldsCallback(int fieldscount) {
            this.currentRow = 0;
            this.fieldsCount=fieldscount;
        }

        @Override
        public void call(LinkedList<String> list) {
            if (currentRow>0)
                System.out.println("Detect fields type. Row #"+currentRow);

            lineProcessor(list, currentRow,fieldsCount);
            currentRow++;
        }
    }

    private class DataImportCallBack implements IParserCallBack {
        private long currentRow;
        private int fieldsCount;

        @Override
        public void call(LinkedList<String> list) {
            ++currentRow;

            if (currentRow>0)
            System.out.println("Import row №"+currentRow);

            lineImporter(list, currentRow,fieldsCount);

            if (currentRow>0)
                rowCount++;
        }

        public DataImportCallBack(int fieldsCount) {
            this.fieldsCount=fieldsCount;
            this.currentRow=-1;
        }

    }


}

