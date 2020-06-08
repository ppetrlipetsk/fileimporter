package tableslib;

import defines.FieldTypeDefines;

public class FieldRecord{
    private String header;
    private String alias;
    private String value;
    private FieldTypeDefines.FieldType fieldType;

    public String getHeader() {
        return header;
    }

    public void setHeader(String header) {
        this.header = header;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }


    public FieldTypeDefines.FieldType getFieldType() {
        return fieldType;
    }

    @Override
    public String toString(){
        return "alias="+alias+" type="+fieldType.toString();
    }

    public void setFieldType(FieldTypeDefines.FieldType fieldType) {
        this.fieldType = fieldType;
    }

    // Сравнивает типы по приоритетам, без тождественностии. Сравнивается именно приоритет, а не значение приоритета.
    // Так, STRINGTYPE имеет приоритет 0, и он самый высокий. Таким образом, если c1>c2, то приоритет c1 будет ниже, чем у c2.
    // Результат:
    // 1-если c1>c2,
    // -1, если c1<c2
    // 0- если c1=c2
    public static int compareTypes(FieldTypeDefines.FieldType c1, FieldTypeDefines.FieldType c2){

        if (c1==null) return -1;

        if (FieldTypeDefines.getFieldTypePriority().get(c1)>FieldTypeDefines.getFieldTypePriority().get(c2)) return -1;
        else
        if (FieldTypeDefines.getFieldTypePriority().get(c1)<FieldTypeDefines.getFieldTypePriority().get(c2)) return 1;
        else
            return 0;
    }

    // Сравнивает типы по приоритетам, с проверкой тождественностии.
    // Результат:
    // 1-если c1>c2,
    // -1, если c1<c2
    // 0- если c1===c2
    // 3, если приоритеты равны, но типы разные
    public static int compareTypesEnhanced(FieldTypeDefines.FieldType c1, FieldTypeDefines.FieldType c2){
        int res=(compareTypes(c1,c2));
        if (res==0)
            if (c1==c2){ return 0;}
            else
                return 3;
        return res;
    }

    // Возвращает тип поля, с наибольшим приоритетом. Если типы нетождественно равны, то тип надо установить в STRINGTYPE.
    public static FieldTypeDefines.FieldType getTypeByPriority(FieldTypeDefines.FieldType c1, FieldTypeDefines.FieldType c2){
        if (compareTypesEnhanced(c1,c2)==1) return c1;
        else
        if (compareTypesEnhanced(c1,c2)==-1) return c2;
        else
        if (compareTypesEnhanced(c1,c2)==3) return FieldTypeDefines.FieldType.STRINGTYPE;
        return c1;
    }

    public FieldRecord(String header, String alias, String value, FieldTypeDefines.FieldType fieldType) {
        this.header = header;
        this.alias = alias;
        this.value = value;
        this.fieldType=fieldType;
    }

}