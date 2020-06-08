set tablename=zmm_importer
set filename=c://files//tmc//xls//zmm.xlsx
set delimiter=@
set fieldscount=287
set linedelimiter=true
set storealiases=true
set createtable=true
set applog=zmm_applog.log 
set errorlog=zmm_errorlog.log
java -jar tmc_file_importer-1.0-SNAPSHOT-jar-with-dependencies.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount% linedelimiter=%linedelimiter%  storealiases=%storealiases%  createtable=%createtable% applog=%applog% errorlog=%errorlog%

pause