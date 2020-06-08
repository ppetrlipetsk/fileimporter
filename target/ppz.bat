set tablename=ppz
set filename=c://files//tmc//xls//ppz.xlsx
rem set filename=c://files//tmc//xls//zmm_short.xlsx
set fieldscount=287
set storealiases=true
set createtable=true
set applog=ppz_applog.log 
set errorlog=ppz_errorlog.log
set importtable=false 
set tabledropnonprompt=true
java -jar tmc_file_importer-1.0-SNAPSHOT-jar-with-dependencies.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount%   storealiases=%storealiases%  createtable=%createtable% applog=%applog% errorlog=%errorlog% importtable=%importtable% tabledropnonprompt=%tabledropnonprompt%

pause