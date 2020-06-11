package environment;

import com.ppsdevelopment.configlib.ConfigReader;

public class ApplicationConfig {
    public static boolean initApplicationValues() {
        if (!com.ppsdevelopment.configlib.ConfigReader.readConfig("config.ini")) return false;
        DBSettings.databaseName=ConfigReader.getPropertyValue("databaseName");
        DBSettings.instanceName=ConfigReader.getPropertyValue("instanceName");
        DBSettings.pass=ConfigReader.getPropertyValue("password");
        DBSettings.userName=ConfigReader.getPropertyValue("userName");
        return true;
    }

}

