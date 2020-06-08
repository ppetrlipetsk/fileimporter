package tableslib;

import defines.FieldTypeDefines;
import defines.FieldsDefines;
import defines.defaultValues;
import throwlib.FieldTypeError;
import typeslib.FieldConvertClass;
import throwlib.FieldTypeCorrectionError;
import typeslib.DetectTypeClass;

import java.net.ConnectException;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;



public class TableInfo {
    private final String tableName;
    private final String fileName;
    private final int fieldsCount;
    private final boolean lineDelimiter;
    private final boolean storeAliases;
    private final boolean createDBTable;

    FieldsMap fields=new FieldsMap(16, 0.75f,false);
    private int recordsCount;

    public void setFields(FieldsMap fields) {
        this.fields = fields;
    }

    public void setRecordsCount(int recordsCount) {
        this.recordsCount = recordsCount;
    }

    public TableInfo(String tableName, String fileName, int fieldsCount, boolean lineDelimiter, boolean storealiases, boolean createdbtable) {
        this.tableName = tableName;
        this.fileName = fileName;
        this.fieldsCount = fieldsCount;
        this.lineDelimiter = lineDelimiter;
        this.storeAliases=storealiases;
        this.createDBTable=createdbtable;
    }

    public boolean isCreateDBTable() {
        return createDBTable;
    }

    public boolean isStoreAliases() {
        return storeAliases;
    }

    public boolean isLineDelimiter() {
        return lineDelimiter;
    }

    public String getTableName() {
        return tableName;
    }

    public String getFileName() {
        return fileName;
    }

    public int getFieldsCount() {
        return fieldsCount;
    }

    public FieldsMap getFields() {
        return fields;
    }

    public void generateFields(String header, String dataLine) {

    }

    public void preGenerateFields(String headerLine, String dataLine, boolean lineDelimiter) {
        String[] record=dataLine.split(defaultValues.getDELIMITER());
        String[] header=headerLine.split(defaultValues.getDELIMITER());

        for (int i=0;i<fieldsCount;i++){
            String val;
            if (i<record.length) {
                val = record[i];
            }
            else val="";
//            if(!((lineDelimiter)&&(i==(record.length-1))))
//                addField(header[i],val, tableName);
        }
    }

    public void correctFieldsType(String dataLine, boolean lineDelimiter) throws FieldTypeCorrectionError {
            String[] records=dataLine.split(defaultValues.getDELIMITER());
                correctLineRecordsType(records,fields,lineDelimiter);

            }
/*
    public boolean createTable(){
        boolean isLineDelimiter=this.isLineDelimiter();
        boolean code;
        try {
            long  tableId= 0;
            tableId = getTableId(this.tableName);
            if (this.createDBTable) tableslib.TableClass.createTable(fields,this.tableName,isLineDelimiter);
            if (this.storeAliases) tableslib.TableClass.insertAliases(fields.getFields(),"aliases", tableId);
            code=true;
        } catch (SQLException e) {
            e.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка создания таблицы "+e.getMessage(),true);
            code=false;
        } catch (ConnectException e) {
            e.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка создания таблицы "+e.getMessage(),true);
            code=false;
        } catch (FieldTypeError fieldTypeError) {
            fieldTypeError.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка создания таблицы "+fieldTypeError.getMessage(),true);
            code=false;
        }
        return code;
    }
*/
    private long getTableId(String tableName) throws SQLException {
        long  tableId=tableslib.TableClass.getTableId(tableName);
        if ((tableId==-1)&&(isStoreAliases()))
            tableId=tableslib.TableClass.insertTable(tableName);
        return tableId;
    }

    private  void correctLineRecordsType(String[] line, FieldsMap fields, boolean lineDelimiter) throws FieldTypeCorrectionError {
        Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
        try {
            int i=0;
            int ld=lineDelimiter?1:0;
            int fieldsCount=line.length-ld;

            while ((itr1.hasNext()) && (i < (fieldsCount))) {
                String value = line[i];
                Map.Entry<String, FieldRecord> entry = itr1.next();

                FieldTypeDefines.FieldType currentFieldType = entry.getValue().getFieldType();
                FieldTypeDefines.FieldType newFieldType = DetectTypeClass.getFieldType(value);

                int ct = FieldRecord.compareTypesEnhanced(currentFieldType, newFieldType);
                if ((ct != 0) && (ct != 1) && (value.length() > 0)) {
                    setCorrectionFieldType(fields.getFields(), entry.getValue().getAlias(), FieldRecord.getTypeByPriority(currentFieldType, newFieldType));
                }
                i++;
            }
        }
        catch (Exception e){
            throw new FieldTypeCorrectionError("Ошибка изменения типа записи!");
        }
    }

    private  void setCorrectionFieldType(LinkedHashMap<String,FieldRecord> fields, String fieldName, FieldTypeDefines.FieldType fieldType) {
        FieldRecord field=fields.get(fieldName);
        field.setFieldType(fieldType);
        fields.put(fieldName,field);
    }

// Private block

    private void addField(String header, String value, String tableName) {
        String alias=FieldConvertClass.transLiterate(header);
        String fieldName=getFieldNameByAlias(alias);
        String fieldNameDefines=tableName+"&"+alias;
        boolean b;//=FieldsDefines.fields.containsKey(fieldNameDefines);
        FieldTypeDefines.FieldType fieldType;
//        if (!b)
//            fieldType=getFieldTypeByStr(value);
//        else
//            fieldType=FieldsDefines.fields.get(fieldNameDefines);
        //FieldRecord fieldRecord=new FieldRecord(header,fieldName,value,fieldType);
        //fields.getFields().put(fieldName,fieldRecord);
    }

    private FieldTypeDefines.FieldType getFieldTypeByStr(String s){
        return DetectTypeClass.getFieldType(s);
    }

    private String getFieldNameByAlias(String alias) {
        if (alias!=null){
            if (alias.length()==0) alias="field";
            if (fields.getFields().containsKey(alias)) alias= generateUniqueField(alias,fields);
        }
        return alias;
    }

    // Генерирует уникальное имя поля.
    private String generateUniqueField(String alias, FieldsMap fields) {
        int fieldIndex=0;
        StringBuilder s=new StringBuilder(alias);
        boolean b=true;
        while(b) {
            String indxStr=new Integer(fieldIndex).toString();
            s.append(indxStr);
            if (fields.getFields().containsKey(s.toString())) {
                s.delete(alias.length(),s.length());
                fieldIndex++;
            }
            else b=false;
        }
        return s.toString();
    }


    public int getRecordsCount() {
        return recordsCount;
    }
}
