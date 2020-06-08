set tablename=zmm
set filename=c://files//tmc//xls//file.xlsx
set delimiter=@
set fieldscount=287
set linedelimiter=true
set storealiases=true
set createtable=true
set applog=zmm_applog.log 
set errorlog=zmm_errorlog.log
java -jar tmc_file_importer-1.0-SNAPSHOT-jar-with-dependencies.jar 

pause