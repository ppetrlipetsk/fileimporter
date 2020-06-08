package loglib;

import java.sql.SQLException;

public class ErrorsClass {
    public static void fieldReadErrorLog(SQLException e) throws SQLException {
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего измененные записи таблицы." , true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего измененные записи таблицы. \n"+e.getMessage(), true);
        throw new SQLException(e);
    }

    public static void deletedRecordsReadError(SQLException e) throws SQLException {
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего удаленные записи таблицы." , true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка чтения поля IDN записи БД, при чтении записей представления, содержащего  удаленные записи таблицы. \n"+e.getMessage(), true);
        throw new SQLException(e);
    }

    public static void addedRecordsViewReadError(SQLException e, String query) throws SQLException {
        e.printStackTrace();
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка чтения представления, содержащего добавленные записи.", true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка чтения представления, содержащего добавленные записи.\n"+e.getMessage()+"\n QUERY="+query, true);
        throw new SQLException(e);
    }

    public static void deletedRecordsViewReadError(SQLException e, String query) throws SQLException {
        e.printStackTrace();
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка чтения представления, содержащего удаленные записи.", true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка чтения представления, содержащего удаленные записи.\n"+e.getMessage()+"\n QUERY="+query, true);
        throw new SQLException(e);
    }

    public static void changeRecordsError(SQLException e, String query) {
        e.printStackTrace();
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка изменения записи в БД.", true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка изменения записи в БД."+e.getMessage()+"\n QUERY="+query, true);
    }

    public static void recordUpdateError(SQLException e, String idn) throws SQLException {
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка изменения записи таблицы. IDN="+idn , true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка изменения записи таблицы. IDN="+idn+"\n"+e.getMessage(), true);
        throw new SQLException(e);
    }

    public static void recordInsertError(SQLException e) {
        e.printStackTrace();
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка вставки записи таблицы.", true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка вставки записи таблицы."+e.getMessage(), true);
    }

    public static void tableClassNewInstanceError() {
        Logger.putLineToLogs(new String[] {Logger.APPLOG}, "Ошибка создания экземпляра класса таблицы.", true);
        Logger.putLineToLogs(new String[] {Logger.ERRORLOG}, "Ошибка создания экземпляра класса таблицы.", true);
    }
}
