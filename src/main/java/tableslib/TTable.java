package tableslib;

public class TTable {
    String tablename;
    String filename;
    int fieldscount;
    boolean linedelimiter;
    boolean storealiases;
    boolean createtable;

    public TTable(String tablename, String filename, int fieldscount, boolean linedelimiter, boolean storealiases, boolean createtable) {
        this.tablename=tablename;
        this.filename=filename;
        this.fieldscount=fieldscount;
        this.linedelimiter=linedelimiter;
        this.storealiases=storealiases;
        this.createtable=createtable;
    }


    public FieldsMap loadFields() {
        return null;
    }
}
