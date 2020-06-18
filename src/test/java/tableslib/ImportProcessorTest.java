package tableslib;

import com.ppsdevelopment.programparameters.ProgramParameters;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldRecord;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldType;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.FieldsCollection;
import environment.ApplicationGlobals;
import org.junit.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ImportProcessorTest  {

    private static void initProgramParametersValues(){
        ProgramParameters.setParameterProperties("filename","zmm_short.xlsx",true);
        ProgramParameters.setParameterProperties("tablename","zmm",true);
        ProgramParameters.setParameterProperties("fieldscount","287",true);
        ProgramParameters.setParameterProperties("storealiases","false",false);
        ProgramParameters.setParameterProperties("createtable","false",false);
        ProgramParameters.setParameterProperties("importtable","true",true);
        ProgramParameters.setParameterProperties("tabledropnonprompt","false",false);
        ProgramParameters.setParameterProperties("fieldsfile","zmm.ini",false);
        ProgramParameters.setParameterProperties(ApplicationGlobals.getAPPLOGName(),"",true);
        ProgramParameters.setParameterProperties(ApplicationGlobals.getERRORLOGName(),"",true);
    }

    private static ImportProcessor importProcessorSecondaryTableInstance(){
        //tablename=zmm filename=c://files//tmc//xls//zmm_short.xlsx fieldscount=287 storealiases=false  createtable=false  applog=zmm_applog.log   errorlog=zmm_errorlog.log  importtable=true
        String filename = ProgramParameters.getParameterValue("filename");
        String tablename = ProgramParameters.getParameterValue("tablename");
        int fieldscount = Integer.valueOf(ProgramParameters.getParameterValue("fieldscount"));
        Boolean storealiases = Boolean.valueOf(ProgramParameters.getParameterValue("fieldscount"));
        Boolean createtable = Boolean.valueOf(ProgramParameters.getParameterValue("createtable"));
        Boolean importtable = Boolean.valueOf(ProgramParameters.getParameterValue("importtable"));
        Boolean tabledropnonprompt = Boolean.valueOf(ProgramParameters.getParameterValue("tabledropnonprompt"));
        ImportProcessor importProcessor= new ImportProcessor(filename, tablename, fieldscount, storealiases, createtable, importtable, tabledropnonprompt);
        return importProcessor;
    }

    private void initSource(FieldsCollection f){
        f.put("field1", new FieldRecord("field1","field1","field1", FieldType.INTTYPE));
        f.put("field2", new FieldRecord("field2","field2","field2", FieldType.STRINGTYPE));
    }

    @org.junit.Test
    public void testValidateFieldsAndAliases_Check() {
        initProgramParametersValues();

        ImportProcessor processor=importProcessorSecondaryTableInstance();
        FieldsCollection sourceFields=new FieldsCollection(16, 0.75f,false);;
        FieldsCollection destinationFields=new FieldsCollection(16, 0.75f,false);

        initSource(sourceFields);
        initSource(destinationFields);

        validateFieldsandAliases1(processor, sourceFields, destinationFields);

        validateFieldsAndAliases2(processor, sourceFields, destinationFields);

        validateFieldsAndAliases3(processor, sourceFields, destinationFields);

        validateFieldsAndAliases4(processor, sourceFields, destinationFields);

        validateFieldsAndAliases5(processor, sourceFields, destinationFields);

    }

    private void validateFieldsAndAliases5(ImportProcessor processor, FieldsCollection sourceFields, FieldsCollection destinationFields) {
        boolean actual;
        System.out.print("Поле в таблице-назначения не существует:");
        destinationFields.put("field4", new FieldRecord("field1","field1","field1", FieldType.STRINGTYPE));
        try {
            actual=processor.validateFieldsAndAliases_Check(sourceFields,destinationFields);
        } catch (ImportTableException e) {
            System.out.println(e.toString());
            actual=false;
        }
        assertEquals(false,actual);
        System.out.println("Ok");
    }

    private void validateFieldsAndAliases4(ImportProcessor processor, FieldsCollection sourceFields, FieldsCollection destinationFields) {
        boolean actual;
        System.out.print("Поле в таблице-источнике не существует:");
        sourceFields.put("field3", new FieldRecord("field1","field1","field1", FieldType.STRINGTYPE));
        try {
            actual=processor.validateFieldsAndAliases_Check(sourceFields,destinationFields);
        } catch (ImportTableException e) {
            System.out.println(e.toString());
            actual=false;
        }
        assertEquals(true,actual);
        System.out.println("Ok");
    }

    private void validateFieldsAndAliases3(ImportProcessor processor, FieldsCollection sourceFields, FieldsCollection destinationFields) {
        boolean actual;
        System.out.print("Source=null:");
        sourceFields.put("field1", new FieldRecord("field1","field1","field1", FieldType.STRINGTYPE));
        try {
            actual=processor.validateFieldsAndAliases_Check(null,destinationFields);
        } catch (ImportTableException e) {
            System.out.println(e.toString());
            actual=false;
        }
        assertEquals(false,actual);
        System.out.println("Ok");
        sourceFields.put("field1", new FieldRecord("field1","field1","field1", FieldType.INTTYPE));
    }

    private void validateFieldsAndAliases2(ImportProcessor processor, FieldsCollection sourceFields, FieldsCollection destinationFields) {
        boolean actual;
        System.out.print("Типы не совпадают:");
        sourceFields.put("field1", new FieldRecord("field1","field1","field1", FieldType.STRINGTYPE));
        try {
            actual=processor.validateFieldsAndAliases_Check(sourceFields,destinationFields);
        } catch (ImportTableException e) {
            System.out.println(e.toString());
            actual=false;
        }
        assertEquals(false,actual);
        System.out.println("Ok");
        sourceFields.put("field1", new FieldRecord("field1","field1","field1", FieldType.INTTYPE));
    }

    private void validateFieldsandAliases1(ImportProcessor processor, FieldsCollection sourceFields, FieldsCollection destinationFields) {
        boolean actual;
        System.out.print("Типы совпадают:");
        try {
            actual=processor.validateFieldsAndAliases_Check(sourceFields,destinationFields);
        } catch (ImportTableException e) {
            System.out.println(e.toString());
            actual=false;
        }
        assertEquals(true,actual);
        System.out.println("Ok");
    }

}