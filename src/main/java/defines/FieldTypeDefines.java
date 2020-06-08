package defines;

import java.util.HashMap;

public class FieldTypeDefines {

    public static HashMap<FieldType,String> typesFieldDBStr=new HashMap<>();
    public static HashMap<FieldType,String> typesFieldDBMask=new HashMap<>();
    public static HashMap<FieldType,String> defaultValuesForType=new HashMap<>();
    public static HashMap<FieldType,Integer> fieldTypePriority=new HashMap<>(); // Приоритеты типов. Самый высокий=0;


    static{
        typesFieldDBStr.put(FieldType.DATETYPE,"[date]");
        typesFieldDBStr.put(FieldType.DECIMALTYPE,"[decimal](16, 10)");
        typesFieldDBStr.put(FieldType.FLOATTYPE,"[float]");
        typesFieldDBStr.put(FieldType.STRINGTYPE,"[varchar](1000)");
        typesFieldDBStr.put(FieldType.LONGSTRINGTYPE,"[varchar](5000)");
        typesFieldDBStr.put(FieldType.INTTYPE,"[int]");
        typesFieldDBStr.put(FieldType.BIGINTTYPE,"[bigint]");

        typesFieldDBMask.put(FieldType.DATETYPE,"'@value@'");
        typesFieldDBMask.put(FieldType.FLOATTYPE,"@value@");
        typesFieldDBMask.put(FieldType.DECIMALTYPE,"@value@");
        typesFieldDBMask.put(FieldType.STRINGTYPE,"'@value@'");
        typesFieldDBMask.put(FieldType.LONGSTRINGTYPE,"'@value@'");
        typesFieldDBMask.put(FieldType.INTTYPE,"@value@");
        typesFieldDBMask.put(FieldType.BIGINTTYPE,"@value@");

        defaultValuesForType.put(FieldType.DATETYPE,"01.01.2000");
        defaultValuesForType.put(FieldType.FLOATTYPE,"0");
        defaultValuesForType.put(FieldType.DECIMALTYPE,"0");
        defaultValuesForType.put(FieldType.STRINGTYPE,"");
        defaultValuesForType.put(FieldType.LONGSTRINGTYPE,"");
        defaultValuesForType.put(FieldType.INTTYPE,"0");
        defaultValuesForType.put(FieldType.BIGINTTYPE,"0");

        fieldTypePriority.put(FieldType.DATETYPE,6);
        fieldTypePriority.put(FieldType.DECIMALTYPE,3);
        fieldTypePriority.put(FieldType.FLOATTYPE,2);
        fieldTypePriority.put(FieldType.STRINGTYPE,1);
        fieldTypePriority.put(FieldType.LONGSTRINGTYPE,0);
        fieldTypePriority.put(FieldType.INTTYPE,5);
        fieldTypePriority.put(FieldType.BIGINTTYPE,4);
    }

    public static HashMap<FieldType, String> getTypesFieldDBStr() {
        return typesFieldDBStr;
    }

    public static HashMap<FieldType, String> getTypesFieldDBMask() {
        return typesFieldDBMask;
    }

    public static String getFieldDBStrByType(FieldType t){
        return typesFieldDBStr.get(t);
    }

    public static String getFieldMaskStrByType(FieldType t){
        return typesFieldDBMask.get(t);
    }

    public static String getDefaultValueForType(FieldType t){
        return defaultValuesForType.get(t);
    }

    public static HashMap<FieldType, String> getDefaultValuesForType() {
        return defaultValuesForType;
    }

    public static HashMap<FieldType, Integer> getFieldTypePriority() {
        return fieldTypePriority;
    }

    public enum FieldType{
        INTTYPE, BIGINTTYPE, STRINGTYPE, DECIMALTYPE, DATETYPE, FLOATTYPE, LONGSTRINGTYPE;
    }

    class TypeDefinitions{
        String mask;
        String typeDBStr;

        public TypeDefinitions(String mask, String typeDBStr) {
            this.mask = mask;
            this.typeDBStr = typeDBStr;
        }
    }


}
