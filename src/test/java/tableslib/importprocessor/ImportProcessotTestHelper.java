package tableslib.importprocessor;

import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldRecord;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldType;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsCollection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.*;

public class ImportProcessotTestHelper {

    public static boolean checkImportetData(String tablename, FieldsCollection fields) {
        System.out.print("Проверка данных таблицы exampes1:");
        ArrayList<HashMap<String, Object>> lines=loadDataFromDB(tablename, fields);
        HashMap<Integer,HashMap<String, Object>> sampleData=DataSetSample.getSampleData();
        boolean result=compareDataSet(lines,sampleData,fields);
        System.out.println((result?"OK":"FAILED"));
        return result;
    }

    private static boolean compareDataSet(ArrayList<HashMap<String, Object>> lines, HashMap<Integer, HashMap<String, Object>> sampleData,FieldsCollection fields) {
        boolean c=true;
        for(HashMap<String, Object> line:lines){
            c=checkLine(line,sampleData,fields);
            if (!c) break;
        }
        return c;
    }

    private static boolean checkLine(HashMap<String, Object> line, HashMap<Integer, HashMap<String, Object>> sampleData, FieldsCollection fields) {
        int idn=(int)line.get("idn");
        HashMap<String, Object> sampleLine=sampleData.get(idn);
        boolean c1=true;
        for (Map.Entry<String, Object> entry : line.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            FieldType t = fields.get(key).getFieldType();
            try {
                c1 = c1 && fieldsEquals(value, sampleLine.get(key), t);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (!c1)
                break;
        }
        return c1;
    }

    private static <T> boolean valuesEquals(T i1, T i2){
        if ((i1==null)&&(i2==null)) return true;
        if ((i1==null)||(i2==null)) return false;
        if (i1.getClass()==Double.class) return ((Double)i1).compareTo((Double)i2)==0;
        return i1==i2;
    }

    private static boolean fieldsEquals(Object value, Object sample, FieldType fieldType) throws Exception {
        if (FieldType.INTTYPE==fieldType) return valuesEquals(value,Integer.valueOf((String)sample));
        if ((FieldType.BIGINTTYPE==fieldType)||(FieldType.STRINGTYPE==fieldType)||(FieldType.LONGSTRINGTYPE==fieldType)) return (String.valueOf(value)).equals(String.valueOf(sample));
        if ((FieldType.FLOATTYPE==fieldType)||(FieldType.DECIMALTYPE==fieldType)) return valuesEquals ( value, Double.valueOf(String.valueOf(sample)));

        if (FieldType.DATETYPE==fieldType) {
            String s1=null;
            try {
                s1= (com.ppsdevelopment.converters.DateFormatter.convertDateFormat(value.toString(), "yyyy-MM-dd", "dd.MM.yyyy", null));
            } catch (ParseException e) {
                e.printStackTrace();
            }
                String s2=(String)sample;
            return s2.equals(s1);
        }
        throw new Exception("Нет определения для сравнения типа поля:"+fieldType.toString());
    }

    private static ArrayList<HashMap<String, Object>> loadDataFromDB(String tablename, FieldsCollection fields) {
        ArrayList<HashMap<String, Object>> lines=new ArrayList<>();
        HashMap<String, Object> values=new HashMap<>();
        String query="select * from "+tablename;
        try (DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection()))
        {
            ResultSet resultSet=dp.query(query);
            if ((resultSet != null)) {
                while (resultSet.next()) {
                    Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getIterator();
                    while ((itr1.hasNext())) {
                        Map.Entry<String, FieldRecord> entry = itr1.next();
                        String key=entry.getKey();
                        values.put(key,resultSet.getObject(key));
                    }
                    lines.add(values);
                }
            }
        } catch (SQLException e) {
            System.out.println("Ошибка выполнения запроса к БД."+query+"\n"+e.toString());
        }
        return lines;
    }
}
