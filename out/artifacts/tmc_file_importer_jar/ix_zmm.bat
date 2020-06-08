set tablename=zmm_table1
set filename=c://files//tmc//zmm.csv
set delimiter=@
set fieldscount=287
set linedelimiter=true
set storealiases=true

java -jar tmc_file_importer.jar  tablename=%tablename% filename=%filename% fieldscount=%fieldscount% delimiter=%delimiter% linedelimiter=%linedelimiter%   storealiases=%storealiases%
