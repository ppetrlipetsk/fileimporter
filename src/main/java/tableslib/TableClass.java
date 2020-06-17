package tableslib;
/*
Класс для работы с таблицей
 */
import com.ppsdevelopment.converters.DateFormatter;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.jdbcprocessor.QueryPreparer;
import com.ppsdevelopment.loglib.Logger;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldRecord;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldType;
import com.ppsdevelopment.tmcprocessor.typeslib.FieldTypeDefines;

import environment.QueryRepository;
import throwlib.DateFormatError;
import throwlib.FieldTypeError;

import java.net.ConnectException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;

import static environment.QueryRepository.getAliasInsertQuery;
import static environment.QueryRepository.getIdTableFromTables;
import static environment.QueryRepository.getRecordInsertQuery;


public class TableClass {


    private static String getfieldCreateStatement(String fieldName, String fieldType) {
        StringBuilder s = new StringBuilder();
        s.append(",[").append(fieldName).append("] ").append(fieldType);
        return s.toString();
    }

    public static String getCreateStatement(LinkedHashMap<String, FieldRecord> records, String tableName) {
        StringBuilder s = new StringBuilder();
        s.append("CREATE TABLE [dbo].[");
        s.append(tableName);
        s.append("] ([id] [int] IDENTITY(1,1) NOT NULL ");

        Iterator<Map.Entry<String, FieldRecord>> itr1 = records.entrySet().iterator();
        while (itr1.hasNext()) {
            Map.Entry<String, FieldRecord> entry = itr1.next();
            if (!(entry.getKey().equals("linedelimiter")))
                s.append(getfieldCreateStatement((entry.getValue()).getAlias(), (FieldTypeDefines.getFieldTypeStr((entry.getValue()).getFieldType()))));
        }
        s.append(") ON [PRIMARY]");
        return s.toString();
    }

    public static void createTable(LinkedHashMap<String, FieldRecord> records, String tableName) throws SQLException, ConnectException {
        String query = getCreateStatement(records, tableName);
        DataBaseProcessor dbp=new DataBaseProcessor();
        dbp.exec(query);
    }

    public static void insertAliases(LinkedHashMap<String, FieldRecord> fields, String tableName, long tableId) throws SQLException, FieldTypeError {
        String query = getAliasInsertQuery();
        DataBaseProcessor dbp=new DataBaseProcessor();
        for (Map.Entry entry : fields.entrySet()) {
            //DBEngine.insertPreparedQuery(query, new AliasFiller((FieldRecord) entry.getValue(), tableId));
            dbp.insertPreparedQuery(query, new AliasFiller((FieldRecord) entry.getValue(), tableId),"id");

        }
    }


    public static void insertRecord(FieldsCollection fields, String tableName, LinkedList<String> records) throws SQLException {
        String query = getRecordInsertQuery().replace("@tablename@", tableName);
        RecordInsertQueryFiller filler = new RecordInsertQueryFiller(fields, tableName, records);
        query = query.replace("@fields@", filler.getFieldsNamesQueryString());
        DataBaseProcessor dbp=new DataBaseProcessor();
        try {
            query = query.replace("@values@", filler.getValuesStr());
            dbp.query(query);
        } catch (SQLException e) {
            e.printStackTrace();
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка БД при добавлении записи.", true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Строка записи:" + records.toString(), true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка:" + e.getMessage(), true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "SQL state:" + e.getSQLState(), true);
            throw new SQLException(e);
        } catch (ParseException e) {
            e.printStackTrace();
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка при парсинге строки...", true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Строка записи:" + records.toString(), true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка:" + e.getMessage(), true);
        } catch (FieldTypeError fieldTypeError) {
            fieldTypeError.printStackTrace();
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка типа поля...", true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Строка записи:" + records.toString(), true);
            Logger.getLogger("AppLog").putLineToLogs(new String[]{"AppLog", "ErrorLog"}, "Ошибка:" + fieldTypeError.getMessage(), true);
        }
    }


    public static long getTableId(String iuspt_table) throws SQLException {
        String query = getIdTableQuery().replace("%tablename%", iuspt_table);
        DataBaseProcessor dp=new DataBaseProcessor();
        ResultSet result = dp.query(query);
        long id = -1;
        if (result != null) {
            result.next();
            if (result.getRow() != 0)
                id = result.getInt("id");
            result.close();
        }
        return id;
    }

    public static ResultSet getAliasesForTable(long table_id) throws SQLException {
        String query = getAliasesTableQuery().replace("@tableid@", String.valueOf(table_id));
        DataBaseProcessor dp=new DataBaseProcessor();
        ResultSet result = dp.query(query);
        return result;
    }

    private static String getAliasesTableQuery() {
        return QueryRepository.getAliasRecordsQuery();
    }

    private static String getIdTableQuery() {
        return getIdTableFromTables();
    }

    public static long insertTable(String iuspt_table) {
        String query = getInsertTableQuery().replace("%value1%", iuspt_table);
        long result = -1;
        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            result = dp.insertQuery(query,"id");
            dp.close();
        } catch (Exception e) {
        }
        return result;
    }

    public static FieldType detectFieldType(String fieldtype) {
        return FieldType.valueOf(fieldtype);
    }


    private static String getInsertTableQuery() {
        return QueryRepository.getInsertTableQuery();
    }


    public static void dropTable(String dbTableName) {
        String query = QueryRepository.dropTableQuery().replace("@tablename@", dbTableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            dp.exec(query);
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void deleteAliases(String tableName){
            String query = QueryRepository.deleteFromaliasesQuery().replace("@tablename@", tableName);
            try {
                DataBaseProcessor dp=new DataBaseProcessor();
                dp.exec(query);
            } catch (ConnectException e) {
                e.printStackTrace();
            } catch (SQLException e) {
                e.printStackTrace();
            }
    }


    public static void deleteFromTable(String dbTableName) {
        String query = QueryRepository.deleteFromTableQuery(false).replace("@tablename@", dbTableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            dp.exec(query);
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void insertDeletedField(String dbTableName) {
        String query = QueryRepository.insertDeletedField().replace("@tablename@", dbTableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            dp.exec(query);
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean isTableExist(String dbTableName) throws SQLException, ConnectException {
        boolean res=false;
            String query=QueryRepository.getSchemaTablesQuery().replace("@tablename@",dbTableName);
            DataBaseProcessor dp=new DataBaseProcessor();
            ResultSet result=dp.query(query);
        if (result != null) {
            result.next();
            if (result.getRow() != 0)
                res=true;
        }
            result.close();
        dp.close();
        return res;
    }


    public static void deleteTableAlias(String tableName) {
        String query = QueryRepository.deleteTableAliasQuery(false).replace("@tablename@", tableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor();
            dp.exec(query);
        } catch (ConnectException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
    class AliasFiller implements QueryPreparer {
        FieldRecord values;
        long tableId;

        public AliasFiller(FieldRecord values, long tableId) {
            this.values = values;
            this.tableId = tableId;
        }

        @Override
        public boolean prepareStatement(PreparedStatement preparedStatement) throws SQLException {
            preparedStatement.setLong(1, tableId);
            preparedStatement.setString(2, values.getAlias());
            preparedStatement.setString(3, values.getHeader());
            preparedStatement.setString(4, values.getFieldType().toString());
            return true;
        }
    }


     class RecordInsertQueryFiller implements QueryPreparer {
        private final int BEGINRECORD = 52;
        private final int INTERVAL_RECORD = 1;
        private final int ENDRECORD = BEGINRECORD + INTERVAL_RECORD;

        FieldsCollection fields;
        String tableName;
        LinkedList<String> records;

        public RecordInsertQueryFiller(FieldsCollection fields, String tableName, LinkedList<String> records) {
            this.fields = fields;
            this.tableName = tableName;
            this.records = records;
        }

        @Override
        public boolean prepareStatement(PreparedStatement statement) throws SQLException {
            boolean res = true;

            String valuesStr = null;
            try {
                valuesStr = getValuesStr();
            } catch (FieldTypeError fieldTypeError) {
                fieldTypeError.printStackTrace();
                res = false;
            } catch (ParseException e) {
                e.printStackTrace();
                res = false;
            }
            statement.setString(1, valuesStr);
            return res;
        }

        private String getValueForQuery(FieldRecord field, String value) throws FieldTypeError, ParseException { //throws FieldTypeError, ParseException {
            FieldType f = field.getFieldType();
            if (f == FieldType.DATETYPE) {

            }


            if ((value != null) && value.length() > 0) {
                if (f == FieldType.FLOATTYPE) {
                    value = value.replace(" ", "");
                }

                try {
                    if (validateValue(field, value)) {
                        if (f == FieldType.FLOATTYPE) {
                            if (DetectTypeClass.isRealNumber(value)) return getSQLNumeric(value);
                        } else if (f == FieldType.DATETYPE) {
                            if ((DetectTypeClass.isDateEn(value)))
                                return getSQLDate(normDateStr(value));
                            if (DetectTypeClass.isDate(value))
                                return getSQLDate(value);

                        } else if ((f == FieldType.STRINGTYPE) || (f == FieldType.LONGSTRINGTYPE)) {
                            return quoteString(value);
                        } else
                            return value;
                    }
                } catch (FieldTypeError fieldTypeError) {
                    fieldTypeError.printStackTrace();
                    Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, fieldTypeError.getMessage(), true);
                    throw new FieldTypeError(fieldTypeError.getMessage());
                } catch (DateFormatError dateFormatError) {
                    dateFormatError.printStackTrace();
                }
//                catch (ParseException e) {
//                    e.printStackTrace();
//                    loglib.Logger.putLineToLogs(new String[] {"AppLog","ErrorLog"},e.getMessage(),true);
//                    throw new ParseException(e.getMessage(),0);
//                }
            }
            return FieldTypeDefines.getDefaultValueForType(f);
        }

        private String normDateStr(String value) throws DateFormatError {
            String[] d = value.split("/");
            if ((d != null) && (d.length == 3)) {
                if (d[0].length() == 1) d[0] = "0" + d[0];
                if (d[1].length() == 1) d[1] = "0" + d[1];
                if (d[2].length() == 2) d[2] = "20" + d[2];
                return d[1] + "." + d[0] + "." + d[2];
            } else
                throw new DateFormatError("Неверный формат строки даты!");
        }

        private String quoteString(String s) {
            return s.replace("'", "''");
        }

        private String getSQLDate(String value) {
            try {
                return DateFormatter.convertDateFormat(value, "dd.MM.yyyy", null, null);
            } catch (ParseException e) {
                e.printStackTrace();
                Logger.putLineToLogs(new String[]{"AppLog", "ErrorLog"}, e.getMessage(), true);
            }
            return null;
        }

        private String getSQLNumeric(String value) {
            return value.replace(",", ".");
        }

        /**
         * Метод возвращает подготовленную строку значений для вставки в запрос
         *
         * @return *
         * @throws FieldTypeError
         * @throws ParseException
         */
        public String getValuesStr() throws FieldTypeError, ParseException {
            StringBuilder valuesStr = new StringBuilder();
            int i = 0;
            Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();

            while ((itr1.hasNext()) && (i < records.size())) {
                Map.Entry<String, FieldRecord> entry = itr1.next();
                {

                    if (valuesStr.length() > 0) valuesStr.append(",");
                    FieldType field = entry.getValue().getFieldType();
                    String val = null;
                    // Оба исключения возникают в этом методе. В нем они обработаны, надо их пробросить выше, для вывода в журнал
                    val = getValueForQuery(entry.getValue(), records.get(i));
                    valuesStr.append(FieldTypeDefines.getFieldMaskStrByType(field).replace("@value@", val));
                }
                i++;
            }
            return valuesStr.toString();
        }

        private boolean validateValue(FieldRecord field, String val) throws FieldTypeError {
            if (((field.getFieldType() == FieldType.FLOATTYPE) && !(DetectTypeClass.isRealNumber(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.DATETYPE) && (!DetectTypeClass.isDate(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.INTTYPE) && (!DetectTypeClass.isInt(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.BIGINTTYPE) && (!DetectTypeClass.isInt(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.DECIMALTYPE) && (!DetectTypeClass.isRealNumber(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            return true;
        }

        public String getFieldsNamesQueryString() {
            StringBuilder fieldsString = new StringBuilder();
            int i = 0;

            Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
            while ((itr1.hasNext()) && (i < records.size())) {
                Map.Entry<String, FieldRecord> entry = itr1.next();

                if (fieldsString.length() > 0) fieldsString.append(",");
                String fld = (entry.getValue()).getAlias();
                if (fld.equals("id")) {
                    fld = "idn";
                    entry.getValue().setAlias(fld);
                }
                fieldsString.append("[").append(fld).append("]");

                i++;
            }
            return fieldsString.toString();
        }

    }

