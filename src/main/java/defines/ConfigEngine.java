package defines;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

public class ConfigEngine {
    private static final Properties property = new Properties();


    public static boolean readConfig(String fileName) {
        boolean ret=true;


        try {
            property.load(new FileInputStream(fileName));
        } catch (IOException e) {
            e.printStackTrace();
            loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},"Ошибка чтения файла конфигурации...",true);
            ret=false;
        }

        if (ret) loglib.Logger.getLogger("AppLog").put("Чтение конфигурации:SUCCESS\n");

        return ret;
    }

    public static String getPropertyValue(String propName){
        return property.getProperty(propName);
    }
}
