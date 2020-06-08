package defines;

import databaselib.DBSettings;

import java.io.IOException;

public class ApplicationConfig {
    public static boolean initApplicationValues() {
        if (!ConfigEngine.readConfig("config.ini")) return false;
        DBSettings.databaseName=ConfigEngine.getPropertyValue("databaseName");
        DBSettings.instanceName=ConfigEngine.getPropertyValue("instanceName");
        DBSettings.pass=ConfigEngine.getPropertyValue("password");
        DBSettings.userName=ConfigEngine.getPropertyValue("userName");
/*    } catch (IOException e) {
        e.printStackTrace();
        loglib.Logger.putLineToLogs(new String[] {"ErrorLog","AppLog"},"Ошибка чтения конфигурации...", true);
        return;
        */
    return true;

    }

}

