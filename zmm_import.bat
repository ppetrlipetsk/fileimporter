rem cd target
set tablename=zmm
set filename=c://files//tmc//xls//zmm_short.xlsx
set fieldscount=287
set storealiases=true
set applog=zmm_applog.log 
set errorlog=zmm_errorlog.log
set importtable=true 
set tableoverwrite=true  
set fieldsfile=zmm.ini

java -jar target/tmc_file_importer-1.2.4-SNAPSHOT-jar-with-dependencies.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount%  storealiases=%storealiases%   applog=%applog% errorlog=%errorlog% importtable=%importtable% tableoverwrite=%tableoverwrite%  fieldsfile=%fieldsfile%

pause