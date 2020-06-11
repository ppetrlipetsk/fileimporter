package environment;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Класс хранит набор параметров приложения и предоставляет методы работы с параметрами.
 */

public class ProgramParameters {
    private static Map<String, ProgramParameter> parameters;
    static{
        parameters=new HashMap<>();
    }

    public static Map<String, ProgramParameter> getParameters() {
        return parameters;
    }

    public static void setParameters(Map<String, ProgramParameter> parameters) {
        ProgramParameters.parameters = parameters;
    }

    public static ProgramParameter getProgramParameter(String key){
        if ((parameters!=null)&&(parameters.containsKey(key))) return parameters.get(key);
        else
            return null;
    }

    public static void putProgramParameter(String key, ProgramParameter value){
        if (parameters==null) parameters=new HashMap<>();
        parameters.put(key,value);
    }

    /**
     * Метод проверяет, существует и инициализирован ли параметр или его нужно создать.
     * @param key - ключ параметра
     * @return true, если параметр требует создания или инициализации
     */
    private static boolean isNeedToCreate(String key){
        return  ((!parameters.containsKey(key)) || (parameters.get(key) == null));
    }

    /**
     * Создает и инициализирует параметр с заданным ключем и значениями.
     * @param key - ключ параметра
     * @param value - значение параметра
     * @param require - флаг, если true, то параметр обязательно должно быть в строке параметров.
     */
    private static void createParameter(String key, String value, boolean require) {
       parameters.put(key, new ProgramParameter(value, require));
    }

    public static void setParameterValue(String key, String value){
        if (parameters!=null){
            if (isNeedToCreate(key))
                createParameter(key,value,false);
            else
            getProgramParameter(key).setValue(value);
        }
        else
            throw new NullPointerException("Program parameters storage is not initialized.");
    }

    public static void setParameterRequire(String key, boolean require){
        if (parameters!=null){
            if (isNeedToCreate(key))
                createParameter(key,null,require);
            else
                getProgramParameter(key).setRequire(require);
        }
        else
            throw new NullPointerException("Program parameters storage is not initialized.");
    }

    /**
     * Устанавливает значения свойств параметра программы.
     * @param key -ключ
     * @param value-значение
     * @param require- если true, то параметр обязательный.
     */
    public static void setParameterProperties(String key, String value, boolean require){
        if (parameters!=null){
            if (isNeedToCreate(key))
            createParameter(key,value,require);
            else {
                getProgramParameter(key).setValue(value);
                getProgramParameter(key).setRequire(require);
            }
        }
        else
            throw new NullPointerException("Program parameters storage is not initialized.");
    }

    public static String getParameterValue(String key){
        if (parameters!=null){
            if(parameters.containsKey(key))
                return getProgramParameter(key).getValue();
        }
        else
            throw new NullPointerException("Program parameters storage is not initialized.");
        return null;
    }


    public static boolean parameterExists(String key){
        return (parameters!=null)&&(parameters.containsKey(key));
    }

    public static Iterator<Map.Entry<String, ProgramParameter>> getIterator(){
        return parameters.entrySet().iterator();
    }
}
