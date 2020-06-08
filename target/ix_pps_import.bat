set tablename=zmm_import
set filename=c://files//tmc//pps.csv
set delimiter=@
set fieldscount=287
set linedelimiter=false
set storealiases=false
set createtable=true

java -jar tmc_file_importer-1.0-SNAPSHOT-jar-with-dependencies.jar tablename=%tablename% filename=%filename% fieldscount=%fieldscount% delimiter=%delimiter% linedelimiter=%linedelimiter%  storealiases=%storealiases%  createtable=%createtable%
