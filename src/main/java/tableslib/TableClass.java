package tableslib;
/*
Класс для работы с таблицами
 */
import com.ppsdevelopment.converters.DateFormatter;
import com.ppsdevelopment.jdbcprocessor.DataBaseConnector;
import com.ppsdevelopment.jdbcprocessor.DataBaseProcessor;
import com.ppsdevelopment.jdbcprocessor.QueryPreparer;
import com.ppsdevelopment.tmcprocessor.tmctypeslib.*;
import environment.QueryRepository;
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

class TableClass {


    private static String getfieldCreateStatement(String fieldName, String fieldType) {
        return ",[" + fieldName + "] " + fieldType;
    }

    private static String getCreateStatement(LinkedHashMap<String, FieldRecord> records, String tableName) {
        StringBuilder s = new StringBuilder();
        s.append("CREATE TABLE [dbo].[");
        s.append(tableName);
        s.append("] ([id] [int] IDENTITY(1,1) NOT NULL ");

        for (Map.Entry<String, FieldRecord> entry : records.entrySet()) {
            if (!(entry.getKey().equals("linedelimiter")))
                s.append(getfieldCreateStatement((entry.getValue()).getAlias(), (FieldTypeDefines.getFieldTypeStr((entry.getValue()).getFieldType()))));
        }
        s.append(") ON [PRIMARY]");
        return s.toString();
    }

    static void createTable(LinkedHashMap<String, FieldRecord> records, String tableName) throws SQLException, ConnectException {
        String query = getCreateStatement(records, tableName);
        DataBaseProcessor dbp=new DataBaseProcessor(DataBaseConnector.getConnection());
        dbp.exec(query);
    }

    static void insertAliases(LinkedHashMap<String, FieldRecord> fields, long tableId) throws SQLException {
        String query = getAliasInsertQuery();
        DataBaseProcessor dbp=new DataBaseProcessor(DataBaseConnector.getConnection());
        for (Map.Entry entry : fields.entrySet()) {
            dbp.insertPreparedQuery(query, new AliasFiller((FieldRecord) entry.getValue(), tableId),"id");
        }
    }

    static boolean isTableExists(String tableName) throws SQLException {
        String query = QueryRepository.tableExists(tableName);
        boolean exists = true;
        try (DataBaseProcessor dp = new DataBaseProcessor(DataBaseConnector.getConnection())) {
            ResultSet resultSet = dp.query(query);
            if ((resultSet != null)) {
                while (resultSet.next()) {
                    exists = exists & (resultSet.getInt("res") == 1);
                }
            }
        }
        return exists;
    }


    static long getTableId(String iuspt_table) throws SQLException {
        String query = getIdTableQuery().replace("%tablename%", iuspt_table);
        DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
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

    static FieldsCollection getAliasesForTable(long table_id) throws SQLException {
        FieldsCollection fields=new FieldsCollection(16, 0.75f,false);
        String query = getAliasesTableQuery().replace("@tableid@", String.valueOf(table_id));
        DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
        ResultSet resultSet = dp.query(query);
        if ((resultSet != null)) {
            while (resultSet.next()) {
                String fieldalias = resultSet.getString("fieldalias");
                FieldType fieldType = TableClass.detectFieldType(resultSet.getString("fieldtype"));
                fields.put(fieldalias, new FieldRecord(fieldalias, fieldalias, null, fieldType));
            }
        }
        dp.close();
        return fields;
    }

    private static String getAliasesTableQuery() {
        return QueryRepository.getAliasRecordsQuery();
    }

    private static String getIdTableQuery() {
        return getIdTableFromTables();
    }

    static long insertTable(String iuspt_table) throws SQLException {
        String query = getInsertTableQuery().replace("%value1%", iuspt_table);
        DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
        long result = dp.insertQuery(query,"id");
        dp.close();
        return result;
    }

    private static FieldType detectFieldType(String fieldtype) {
        return FieldType.valueOf(fieldtype);
    }

    private static String getInsertTableQuery() {
        return QueryRepository.getInsertTableQuery();
    }


    static void dropTable(String dbTableName) throws SQLException, ConnectException {
        String query = QueryRepository.dropTableQuery().replace("@tablename@", dbTableName);
        DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
        dp.exec(query);
    }

    static void deleteAliases(String tableName) throws Exception {
            String query = QueryRepository.deleteFromaliasesQuery().replace("@tablename@", tableName);
            try {
                DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
                dp.exec(query);
            } catch (ConnectException | SQLException e) {
                throw new Exception("Ошибка БД при удалении записей из таблицы псевдонимов. Сообщение об ошибке:"+e.toString());
            }
    }

    static void insertDeletedField(String dbTableName) throws Exception {
        String query = QueryRepository.insertDeletedField().replace("@tablename@", dbTableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
            dp.exec(query);
        } catch (ConnectException | SQLException e) {
            throw new Exception("Ошибка вставки поля 'deleted'. Сообщение об ошибке:"+e.toString());
        }
    }

    static boolean isTableExist(String dbTableName) throws SQLException {
        boolean res=false;
            String query=QueryRepository.getSchemaTablesQuery().replace("@tablename@",dbTableName);
            DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
            ResultSet result=dp.query(query);
        if (result != null) {
            result.next();
            if (result.getRow() != 0)
                res=true;
        }
        if (result != null) {
            result.close();
        }
        dp.close();
        return res;
    }


    static void deleteTableRecord(String tableName) throws Exception {
        String query = QueryRepository.deleteTableRecordQuery().replace("@tablename@", tableName);
        try {
            DataBaseProcessor dp=new DataBaseProcessor(DataBaseConnector.getConnection());
            dp.exec(query);
        } catch (ConnectException | SQLException e) {
            throw new Exception("Ошибка БД при удалении записи из таблицы tables. Сообщение об ошибке:"+e.toString());
        }
    }
}
    class AliasFiller implements QueryPreparer {
        private final FieldRecord values;
        private final long tableId;

        AliasFiller(FieldRecord values, long tableId) {
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
        private final FieldsCollection fields;
        private final LinkedList<String> records;

        RecordInsertQueryFiller(FieldsCollection fields,  LinkedList<String> records) {
            this.fields = fields;
            this.records = records;
        }

        @Override
        public boolean prepareStatement(PreparedStatement statement) throws SQLException {
            boolean res = true;
            String valuesStr = null;
            try {
                valuesStr = getValuesStr();
            } catch (FieldTypeError fieldTypeError) {
                //fieldTypeError.printStackTrace();
                res = false;
            }
            statement.setString(1, valuesStr);
            return res;
        }

        private String getValueForQuery(FieldRecord field, String value) throws FieldTypeError {
            FieldType f = field.getFieldType();
            if ((value != null) && value.length() > 0) {
                if (f == FieldType.FLOATTYPE) {
                    value = value.replace(" ", "");
                }
                try {
                    validateValue(field, value);
                    if (f == FieldType.FLOATTYPE) {
                        if (DetectType.isRealNumber(value)) return getSQLNumeric(value);
                    } else if (f == FieldType.DATETYPE) {
                        if ((DetectType.isDateEn(value)))
                            return getSQLDate(normDateStr(value));
                        if (DetectType.isDate(value))
                            return getSQLDate(value);

                    } else if ((f == FieldType.STRINGTYPE) || (f == FieldType.LONGSTRINGTYPE)) {
                        return quoteString(value);
                    } else
                        return value;
                } catch (Exception e) {
                    throw new FieldTypeError("Ошибка формирования строки значений для запроса вставки данных. Сообщение об ошибке:"+e.toString());
                }
            }
            return FieldTypeDefines.getDefaultValueForType(f);
        }

        private String normDateStr(String value) throws Exception {
            String[] d = value.split("/");
            if (d.length == 3) {
                if (d[0].length() == 1) d[0] = "0" + d[0];
                if (d[1].length() == 1) d[1] = "0" + d[1];
                if (d[2].length() == 2) d[2] = "20" + d[2];
                return d[1] + "." + d[0] + "." + d[2];
            } else
                throw new Exception("Неверный формат строки даты! Строка даты:"+value);
        }

        private String quoteString(String s) {
            return s.replace("'", "''");
        }

        private String getSQLDate(String value) throws Exception {
            try {
                return DateFormatter.convertDateFormat(value, "dd-MM-yyyy", null, null);
            } catch (ParseException e) {
                throw new Exception("Ошибка форматирования строки даты. Сообщение об ошибке:"+e.toString());
            }
        }

        private String getSQLNumeric(String value) {
            return value.replace(",", ".");
        }

        /**
         * Метод возвращает подготовленную строку значений для вставки в запрос
         *
         * @return *
         * @throws FieldTypeError
         */
        String getValuesStr() throws FieldTypeError {
            StringBuilder valuesStr = new StringBuilder();
            int i = 0;
            Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getFields().entrySet().iterator();
            while ((itr1.hasNext()) && (i < records.size())) {
                Map.Entry<String, FieldRecord> entry = itr1.next();
                {
                    if (valuesStr.length() > 0) valuesStr.append(",");
                    FieldType field = entry.getValue().getFieldType();
                    String val = getValueForQuery(entry.getValue(), records.get(i));
                    valuesStr.append(FieldTypeDefines.getFieldMaskStrByType(field).replace("@value@", val));
                }
                i++;
            }
            return valuesStr.toString();
        }

        private void validateValue(FieldRecord field, String val) throws FieldTypeError {
            if (((field.getFieldType() == FieldType.FLOATTYPE) && !(DetectType.isRealNumber(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.DATETYPE) && (!DetectType.isDate(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.INTTYPE) && (!DetectType.isInt(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.BIGINTTYPE) && (!DetectType.isInt(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
            else if (((field.getFieldType() == FieldType.DECIMALTYPE) && (!DetectType.isRealNumber(val))))
                throw new FieldTypeError("Ошибка типа поля " + field.toString() + " value=" + val);
        }

        String getFieldsNamesQueryString() {
            StringBuilder fieldsString = new StringBuilder();
            int i = 0;

            Iterator<Map.Entry<String, FieldRecord>> itr1 = fields.getIterator();
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

