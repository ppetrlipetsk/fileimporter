package loglib;

import com.ppsdevelopment.loglib.Logger;
import environment.ApplicationGlobals;

import java.sql.SQLException;

public class ErrorsClass {
    private static final String[] logs={ApplicationGlobals.getAPPLOGName(),ApplicationGlobals.getERRORLOGName()};

    public static void fieldReadErrorLog(SQLException e) throws SQLException {
        Logger.putLineToLogs(logs,"Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего измененные записи таблицы." , true);        
        throw new SQLException(e);
    }

    public static void deletedRecordsReadError(SQLException e) throws SQLException {
        Logger.putLineToLogs(logs, "Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего удаленные записи таблицы." , true);       
        throw new SQLException(e);
    }

    public static void addedRecordsViewReadError(SQLException e, String query) throws SQLException {
        e.printStackTrace();
        Logger.putLineToLogs(logs, "Ошибка чтения представления, содержащего добавленные записи.", true);
        throw new SQLException(e);
    }

    public static void deletedRecordsViewReadError(SQLException e, String query) throws SQLException {
        e.printStackTrace();
        Logger.putLineToLogs(logs, "Ошибка чтения представления, содержащего удаленные записи.", true);
        throw new SQLException(e);
    }

    public static void changeRecordsError(SQLException e, String query) {
        e.printStackTrace();
        Logger.putLineToLogs(logs, "Ошибка изменения записи в БД.", true);
    }

    public static void recordUpdateError(SQLException e, String idn) throws SQLException {
        Logger.putLineToLogs(logs, "Ошибка изменения записи таблицы. IDN="+idn , true);
        throw new SQLException(e);
    }

    public static void recordInsertError(SQLException e) {
        e.printStackTrace();
        Logger.putLineToLogs(logs, "Ошибка вставки записи таблицы.", true);
    }

    public static void tableClassNewInstanceError() {
        Logger.putLineToLogs(logs, "Ошибка создания экземпляра класса таблицы.", true);
    }
}
