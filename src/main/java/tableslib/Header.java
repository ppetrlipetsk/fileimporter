package tableslib;

import com.ppsdevelopment.converters.Transliterate;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.*;
import excelengine.ExcelReader;
import excelengine.IParserCallBack;
import throwlib.FieldTypeError;
import java.net.ConnectException;
import java.sql.SQLException;
import java.util.*;

public class Header {
    private String exceptionMessage;

    public String getExceptionMessage() {
        return exceptionMessage;
    }

    /*
     * При загрузке таблицы есть следующие ситуации:
     *  1. Таблицы загружаются в чистую БД.
     *  2. При загрузке основной таблицы, другая основная таблица с тем же именем, уже загружена
     *  3. При загрузке таблицы изменений, другая таблица изменений с тем же именем, уже существует в БД.
     *  4. При загрузке таблицы изменений, основной таблицы не существует.
     */
    public FieldsCollection loadFields(String tableName, String fileName, boolean importTable, boolean tableOverwrite,  boolean storeAliases, int fieldsCount) throws Exception {
        this.exceptionMessage=null;
        String dbTableName= importTable ? tableName+"_import" : tableName;
        FieldsCollection fieldsSource=null;
        FieldsCollection fieldsDestination=new FieldsCollection(16, 0.75f,false); // Найденные поля
        if (importTable) { //Если это импорт таблицы изменений, в уже имеющуюся таблицу, с определенным набором полей, то загружаем сущействующий набор полей
            fieldsSource=loadFieldsFromDB(tableName);
            if ((fieldsSource!=null)&&(fieldsSource.size()==0)) throw new Exception("Таблица изменений должна загружаться после основной таблицы. Основная таблица не найдена."); //Если не нашли информацию о полях основной таблицы-исключение
        }
        Header.FieldsCallback fcb = new Header.FieldsCallback(fieldsCount,fieldsDestination);
        ExcelReader ereader = new ExcelReader(fileName, fcb, fieldsCount);
        ereader.read();
        ereader.close();
        correctFieldTypes(fieldsDestination);
        if (importTable) {
            validateFieldsAndAliases(fieldsSource, fieldsDestination);
            if (!TableClass.isTableExists(tableName)) throw new Exception("Невозможно импортировать вспомогательную таблицу, т.к. основная таблица не найдена.");
        }
        createTable(fieldsDestination,tableName,dbTableName,tableOverwrite,storeAliases,importTable);
        return fieldsDestination;
    }

    // Загружает поля из БД
    private FieldsCollection loadFieldsFromDB(String tableName) throws SQLException {
        long tableId = getTableId(tableName);
        if (tableId!=-1)
        return TableClass.getAliasesForTable(tableId);
        else
            return null;
    }

    // Проверяет, приемлемость типов полей таблиц.
    // т.е. если поле в основной таблице имеет тип STRINGTYPE, а поле в таблице изменений имеет тип INTPE,
    // то тип INTTYPE может быть успешно импртирован в поле с типом STRINGTYPE.
    // А Поле с типом STRINGTYPE не сможет быть импортировано в поле с типом INTTYPE.Если типы не равны, или такого поля в одной из таблиц нет, то false.
    // Поэтому, надо сравнить типы, и если они приемлемы, то продолжить импорт. Иначе исключение.
    private void validateFieldsAndAliases(FieldsCollection fieldsSource, FieldsCollection destination) throws Exception {
        if ((fieldsSource==null)||(destination==null)){
            Formatter f=new Formatter();
            throw new Exception(f.format("Коллекция псевдонимов полей не существует.").toString());
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
                throw new Exception(f.toString());
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

    private boolean isTableExists(String dbTableName) {
        try {
            return TableClass.isTableExist(dbTableName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private long getTableId(String tableName) throws SQLException {
        return TableClass.getTableId(tableName);
    }

    private void createTable(FieldsCollection fields, String tableName, String dbTableName, boolean tableOverwrite,  boolean storeAliases, boolean importTable) throws Exception {
        try {
            boolean tableExists = isTableExists(dbTableName);
            cleanTable(dbTableName, tableOverwrite, tableExists);
            createDBTable(fields, dbTableName, tableOverwrite, importTable, tableExists);
            createAliases(fields, tableName, storeAliases);
        }
        catch(SQLException | ConnectException | FieldTypeError e){
            Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка создания таблицы " + e.getMessage(), true);
            throw new Exception(e.getMessage());
        }
    }

    private void createAliases(FieldsCollection fields, String tableName, boolean storeAliases) throws SQLException{
        if (storeAliases) {
            long tableId = getTableId(tableName);
            if ((tableId==-1))
                tableId= TableClass.insertTable(tableName);
            TableClass.insertAliases(fields.getFields(), tableId);
        }
    }

    private void createDBTable(FieldsCollection fields, String dbTableName, boolean tableOverwrite, boolean importTable, boolean tableExists) throws Exception {
        if (!tableExists||tableOverwrite) {
            TableClass.createTable(fields.getFields(), dbTableName);
            if (!importTable) TableClass.insertDeletedField(dbTableName);
        }
    }

    private void cleanTable(String dbTableName, boolean tableReCreate, boolean tableExists) throws Exception {
        if (tableExists)
            if (tableReCreate)  //Если таблица существует и ее можно пересоздавать
                {
                    TableClass.deleteAliases(dbTableName);
                    TableClass.deleteTableRecord(dbTableName);
                    TableClass.dropTable(dbTableName);
                }
            else
                throw new Exception("Таблица "+dbTableName+" уже существует. Недостаточно прав на ее уничтожение и создание новой.");
    }

    private void detectFieldsTypes(LinkedList<String> list,FieldsCollection fields) throws Exception {
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


    private String getFieldNameByStr(String cellname, FieldsCollection fields) {
        String alias= Transliterate.toTransliterate(cellname.toLowerCase());
        return getFieldNameByAlias(alias, fields);
    }

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

    private  void correctLineRecordsType(LinkedList<String> line, FieldsCollection fields) throws Exception {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getIterator();
        String value=null;
        String fieldName=null;
        try {
            int i=0;
            int fieldsCount=line.size();
            while ((itr1.hasNext()) && (i < (fieldsCount))) {
                value = line.get(i);
                Map.Entry<String, FieldRecord> entry = itr1.next();
                fieldName=entry.getValue().getAlias();
                boolean b= FieldsDefaults.isFieldExists(entry.getValue().getAlias());//.fields.containsKey(entry.getValue().getAlias());
                if (!b){
                    FieldType currentFieldType = entry.getValue().getFieldType();
                    FieldType newFieldType = DetectType.getFieldType(value);

                    int ct = FieldRecord.compareTypesEnhanced(currentFieldType, newFieldType);
                    if ((ct != 0) && (ct != 1) && (value.length() > 0)) {
                        setCorrectionFieldType(fields.getFields(), entry.getValue().getAlias(), FieldRecord.getTypeByPriority(currentFieldType, newFieldType));
                    }
                }
                else
                    setCorrectionFieldType(fields.getFields(), entry.getValue().getAlias(), FieldsDefaults.getFieldType(entry.getValue().getAlias()));
                i++;
            }
        }
        catch (Exception e){
            throw new Exception("Ошибка изменения типа записи! Значение:"+value+" Имя поля:"+fieldName);
        }
    }

    private  void setCorrectionFieldType(LinkedHashMap<String,FieldRecord> fields, String fieldName, FieldType fieldType) {
        FieldRecord field=fields.get(fieldName);
        field.setFieldType(fieldType);
        fields.put(fieldName,field);
    }

    private void lineProcessor(LinkedList<String> list, long currentRow, int fieldsCount, FieldsCollection fields) {
        if (currentRow==0) createFieldsNameMap(list,fieldsCount,fields);
        else {
            try {
                detectFieldsTypes(list,fields);
            } catch (Exception e) {
                this.exceptionMessage=e.toString();
            }
        }
    }

    private class FieldsCallback implements IParserCallBack {
        private long currentRow;
        private final int fieldsCount;
        private final FieldsCollection fields;


        private FieldsCallback(int fieldscount, FieldsCollection fields) {
            this.currentRow = 0;
            this.fieldsCount=fieldscount;
            this.fields=fields;
        }

        @Override
        public void call(LinkedList<String> list) {
            if (currentRow>0)
                System.out.println("Detect fields type. Row #"+currentRow);
            lineProcessor(list, currentRow,fieldsCount,fields);
            currentRow++;
        }
    }

}
