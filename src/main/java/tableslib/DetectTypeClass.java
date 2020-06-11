package tableslib;

import com.ppsdevelopment.tmcprocessor.typeslib.FieldType;

public class DetectTypeClass {

    static String NUMERIC_REG="^-?\\+?\\d+(\\,+\\d+)?$";
    static String NUMERIC_REG_DOT="^-?\\+?\\d+(\\.+\\d+)?$";
    static String INT_REG="^-?\\+?\\d+?$";
    static String DATE_REG="(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d";
    static String DATE_REG_EN="^([1-9]|1[012])[- /.]([1-9]|[12][0-9]|3[01]){1,2}[- /.](19|20){0,1}(\\d\\d){0,1}$";
    static String STRINGINT_REG="^0\\d+$"; // Строковый тип, но состоит из цифр, начинается с "0"


    public static boolean isStringInt(String s){
        if (s== null) return false;
        return s.matches(STRINGINT_REG);
    }

    public static boolean isRealNumber(String s) {
        if (s.indexOf(",")>-1) s=s.replace(" ","");
        if (s== null) return false;
        return s.matches(NUMERIC_REG);
    }

    public static boolean isRealNumberDot(String string) {
        if (string == null) return false;
        return string.matches(NUMERIC_REG_DOT);
    }

    public static boolean isInt(String string) {
        if (string == null) return false;
        return string.matches(INT_REG);
    }

    public static boolean isDate(String string) {
        if (string == null) return false;
        return string.matches(DATE_REG)||string.matches(DATE_REG_EN);
    }

    public static boolean isDateEn(String string) {
        if (string == null) return false;
        return string.matches(DATE_REG_EN);
    }

    public static FieldType getFieldType(String s) {
        if (s == null) return null;
        if (isStringInt(s)) return FieldType.STRINGTYPE;
        if (isDate(s)) return FieldType.DATETYPE;
        if (isInt(s)){
            if (s.length()>9) return FieldType.BIGINTTYPE;
            else
                return FieldType.INTTYPE;
        }
        else
            if (isRealNumber(s)){
                return FieldType.FLOATTYPE;
            }
            else
                if (s.length()>254) return FieldType.LONGSTRINGTYPE;
        else
            if (s.length()<1000)
                return FieldType.STRINGTYPE;
        else
                return FieldType.LONGSTRINGTYPE;
    }
}
