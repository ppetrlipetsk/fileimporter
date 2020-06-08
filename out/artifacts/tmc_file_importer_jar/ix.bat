set tablename=zmm_table
set filename=c://files//tmc//ZMM_MRP_REPORT_PLAN_PN_@.csv
set delimiter=@
set fieldscount=287

rem java -jar tmcimporter.jar  tablename=ppz_table filename=c://files//tmc//table_ppz.csv fieldscount=287 delimiter=@
rem java -jar C:\Users\96-paliy\IdeaProjects\tmcimporter\out\artifacts\tmcimporter_jar\tmcimporter.jar  tablename=ppz_table filename=c://files//tmc//table_ppz.csv fieldscount=287 delimiter=@ 
java -jar C:\Users\96-paliy\IdeaProjects\tmcimporter\out\artifacts\tmcimporter_jar\tmcimporter.jar  tablename=%tablename% filename=%filename% fieldscount=%fieldscount% delimiter=%delimiter% 
