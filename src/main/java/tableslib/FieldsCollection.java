package tableslib;

import com.ppsdevelopment.tmcprocessor.typeslib.FieldRecord;

import java.util.LinkedHashMap;

/**
 * Класс содержит коллекцию набора полей таблицы БД, типа FieldRecord.
 */
public class FieldsCollection {

    private LinkedHashMap<String, FieldRecord> fields;

    public FieldsCollection(LinkedHashMap<String, FieldRecord> fields) {
        this.fields = fields;
    }

    public FieldsCollection(int i, float v, boolean b) {
        fields=new LinkedHashMap<>(i,v,b);
    }

    public LinkedHashMap<String, FieldRecord> getFields() {
            return fields;
    }

    public void setFields(LinkedHashMap<String, FieldRecord> fields) {
        this.fields = fields;
    }
}
