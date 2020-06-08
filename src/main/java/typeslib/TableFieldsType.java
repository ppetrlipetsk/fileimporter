package typeslib;

import defines.FieldTypeDefines;

import java.util.HashMap;

public class TableFieldsType {
    //private HashMap<String,>
    private HashMap<String, FieldTypeDefines.FieldType> fields;

    public TableFieldsType() {
        fields =new HashMap<>();
    }

    public  HashMap<String, FieldTypeDefines.FieldType> getFields() {
        return fields;
    }

    public  void setFields(HashMap<String, FieldTypeDefines.FieldType> fields) {
        this.fields = fields;
    }


}
