package environment;

public class Settings {
    public static void initProgramParametersValues(){
        ProgramParameters.setParameterProperties("filename","",true);
        ProgramParameters.setParameterProperties("tablename","",true);
        ProgramParameters.setParameterProperties("fieldscount","",true);
        ProgramParameters.setParameterProperties("storealiases","false",false);
        ProgramParameters.setParameterProperties("createtable","false",false);
        ProgramParameters.setParameterProperties("importtable","",true);
        ProgramParameters.setParameterProperties("tabledropnonprompt","false",false);
        ProgramParameters.setParameterProperties("fieldsfile","",false);
        ProgramParameters.setParameterProperties(ApplicationGlobals.APPLOG,"",true);
        ProgramParameters.setParameterProperties(ApplicationGlobals.ERRORLOG,"",true);
    }
}
