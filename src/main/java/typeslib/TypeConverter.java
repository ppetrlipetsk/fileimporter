package typeslib;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TypeConverter {

    // From: dd-MM-yyyy
    //To: yyyy-MM-dd
    private static final String DATE_FORMAT_FROM ="dd-MM-yyyy";
    private static final String DATE_FORMAT_TO ="yyyy-MM-dd";
    public static String convertDateFormat(String fdate,String formatFrom, String formatTo) throws ParseException {
        if (formatFrom==null) formatFrom= DATE_FORMAT_FROM;
        if (formatTo==null) formatTo= DATE_FORMAT_TO;

        DateFormat df=new SimpleDateFormat(formatTo);
        String s=null;
        try {
            Date date=new SimpleDateFormat(formatFrom).parse(fdate);
            s=df.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new ParseException("Ошибка преобразования формата даты: "+fdate+" формат источника:"+formatFrom+" формат назначения:"+formatTo,0);
        }
        return s;
    }
}
