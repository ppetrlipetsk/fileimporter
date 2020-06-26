package environment;

public class QueryRepository {
    public static String getIdTableFromTables(){
        return "select [id] from [dogc].[dbo].[tables] where [tablename]='%tablename%'";
    }

    public static String getInsertTableQuery(){
        return "INSERT INTO [dbo].[tables]" +
                "           ([tablename])" +
                "     VALUES (" +
                "'%value1%')";
    }

    public static String getAliasInsertQuery(){
        return "INSERT INTO [dbo].[aliases] ([table_id],[fieldalias],[fieldname],[fieldtype]) VALUES (?,?,?,?)";
    }
    public static String getRecordInsertQuery(){
        return "INSERT INTO [dbo].[@tablename@] (@fields@) VALUES (@values@)";
    }

    public static String getAliasRecordsQuery(){
        return "select * from [dbo].[aliases] where table_id=@tableid@ order by id";
    }

    public static String getSchemaTablesQuery(){
        return "    SELECT * FROM INFORMATION_SCHEMA.TABLES where table_name='@tablename@'";
    }

    public static String dropTableQuery(){
        return "drop table [dbo].@tablename@";
    }

    public static String deleteFromaliasesQuery(){
        return "delete from aliases where table_id in (select id from tables where tablename='@tablename@')";
    }


    public static String deleteTableRecordQuery() {
        return "delete from tables where tablename='@tablename@'";
    }

    public static String insertDeletedField(){
        //return  "BEGIN TRANSACTION\n" +
        return "ALTER TABLE dbo.@tablename@ ADD\n" +
                "\tdeleted int NULL\n" +
                "ALTER TABLE dbo.zmm ADD CONSTRAINT\n" +
                "\tDF_zmm_deleted DEFAULT 0 FOR deleted\n" +
                "ALTER TABLE dbo.zmm SET (LOCK_ESCALATION = TABLE)\n";
        //+
//                "COMMIT\n";
    }

    public static String tableExists(String tableName){
        return " IF EXISTS (SELECT 1  FROM INFORMATION_SCHEMA.TABLES " +
                " WHERE TABLE_TYPE='BASE TABLE' " +
                " AND TABLE_NAME='"+tableName+"')" +
                " SELECT 1 AS res ELSE SELECT 0 AS res";
    }
}
