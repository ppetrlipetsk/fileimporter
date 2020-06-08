set tablename=zmm
rem set filename=c://files//tmc//xls//zmm.xlsx
set filename=c://files//tmc//xls//zmm_short.xlsx
set fieldscount=287
set storealiases=true
set createtable=true
set applog=zmm_applog.log 
set errorlog=zmm_errorlog.log
set importtable=false 
set tabledropnonprompt=true
set fieldsfile=zmm.ini

java -jar tmc_file_importer-1.0-SNAPSHOT-jar-with-dependencies.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount%   storealiases=%storealiases%  createtable=%createtable% applog=%applog% errorlog=%errorlog% importtable=%importtable% tabledropnonprompt=%tabledropnonprompt% fieldsfile=%fieldsfile%

pause