package typeslib;

import typeslib.TransLiterateClass;

public class FieldConvertClass {
    final static String[][] CHARS={{"/","_"},{"\\","_"},{" ","_"},{"@","_"},{"`","_"},{"\"","_"},{"'","_"},{".","_"},{",","_"},{"№","N"},{"#","N"},{"-","_"},{"(",""},{")",""},{"'","''"}};

    public static String transLiterate(String s) {
        s=s.toLowerCase();
        for (int i=0;i<CHARS.length;i++)
        s=s.replace(CHARS[i][0],CHARS[i][1]);
        s=TransLiterateClass.getTranslitStr(s);
        return s;
    }

}
