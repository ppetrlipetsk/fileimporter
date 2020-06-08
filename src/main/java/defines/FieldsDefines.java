package defines;

import typeslib.TParameter;
import typeslib.TableFieldsType;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class FieldsDefines {

    private static  HashMap<String, TableFieldsType> tableFields;
    //public static HashMap<String, FieldTypeDefines.FieldType> fields =new HashMap<>();

    static {
        tableFields =new HashMap<>();

        TableFieldsType zmm_fields=new TableFieldsType();
        zmm_fields.getFields().put("id",FieldTypeDefines.FieldType.INTTYPE);
        zmm_fields.getFields().put("potrebnost_pen", FieldTypeDefines.FieldType.STRINGTYPE);
        tableFields.put("zmm",zmm_fields);

        TableFieldsType zmm_import_fields=new TableFieldsType();
        zmm_fields.getFields().put("id",FieldTypeDefines.FieldType.INTTYPE);
        zmm_fields.getFields().put("potrebnost_pen", FieldTypeDefines.FieldType.STRINGTYPE);
        tableFields.put("zmm_import",zmm_fields);

        TableFieldsType pps_fields=new TableFieldsType();
        pps_fields.getFields().put("zz_pos_num", FieldTypeDefines.FieldType.STRINGTYPE);
        tableFields.put("pps",pps_fields);

//        fields.put("svodpen_table&naimenovanie_materiala__(polnoe)", FieldTypeDefines.FieldType.LONGSTRINGTYPE);
//        fields.put("svodpen_table&N_zakupki_umts_i_k", FieldTypeDefines.FieldType.STRINGTYPE);
//        fields.put("svodpen_table&kol_vo_zayavleno", FieldTypeDefines.FieldType.FLOATTYPE);
    }

    public static boolean isFieldExists(String tableName, String fieldName){
        boolean b=false;
        TableFieldsType fields=tableFields.get(tableName);
        if (fields!=null) b=fields.getFields().containsKey(fieldName);
        return b;
    }

    public static FieldTypeDefines.FieldType getFieldType(String tableName, String fieldName){
        boolean b=false;
        TableFieldsType fields=tableFields.get(tableName);
        if (fields!=null&&fields.getFields().containsKey(fieldName)) return fields.getFields().get(fieldName);
        return null;
    }


    private static Properties getProperties(String fileName) throws IOException {
        Properties properties = new Properties();
        boolean ret=true;
        properties.load(new FileInputStream(fileName));
        return properties;
    }



    public static boolean loadDefaultFields(String fileName, String tableName) throws IOException {
        Properties properties =getProperties(fileName);
        TableFieldsType fields = new TableFieldsType();
        Set<?> keys=properties.keySet();
        boolean ret=true;

        for (Object key:keys){
            String keyName=(String) key;

            if (!properties.containsKey(key)) throw new NoSuchElementException("Неверный ключ! Key="+keyName);

            String value=(String)properties.getProperty(keyName);

            if (checkFieldType(value)) {
                fields.getFields().put(keyName, FieldTypeDefines.FieldType.valueOf(value));
            }
        }

        tableFields.put(tableName,fields);

        return ret;
    }

    private static boolean checkFieldType(String value) {
        boolean res;
        try {
            FieldTypeDefines.FieldType f = FieldTypeDefines.FieldType.valueOf(value);
            res=true;
        }
        catch (IllegalArgumentException e){
            res=false;
        }
        return res;
    }

}




