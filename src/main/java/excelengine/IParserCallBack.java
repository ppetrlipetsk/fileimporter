package excelengine;

import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsCollection;

import java.util.LinkedList;

public interface IParserCallBack {
    void call(LinkedList<String> list, FieldsCollection fields);
}
