set tablename=pps_table
set filename=c://files//tmc//pps.csv
set delimiter=@
set fieldscount=287
set linedelimiter=false
set storealiases=true
set createtable=true

java -jar C:\Users\96-paliy\IdeaProjects\tmcimporter\out\artifacts\tmcimporter_jar\tmcimporter.jar  tablename=%tablename% filename=%filename% fieldscount=%fieldscount% delimiter=%delimiter% linedelimiter=%linedelimiter%  storealiases=%storealiases%  createtable=%createtable%
