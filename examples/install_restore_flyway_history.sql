-- Restore history --

DROP TABLE "flyway_schema_history";

CREATE TABLE "flyway_schema_history" (
    "installed_rank" NUMBER(*,0), 
	"version" VARCHAR2(50 BYTE), 
	"description" VARCHAR2(200 BYTE), 
	"type" VARCHAR2(20 BYTE), 
	"script" VARCHAR2(1000 BYTE), 
	"checksum" NUMBER(*,0), 
	"installed_by" VARCHAR2(100 BYTE), 
	"installed_on" TIMESTAMP (6) DEFAULT CURRENT_TIMESTAMP, 
	"execution_time" NUMBER(*,0), 
	"success" NUMBER(1,0)
   ) ;

Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('10','9','add transformer disable','SQL','V9__add_transformer_disable.sql','-721781552','MANAGER',to_timestamp('24/09/20 15:25:15,965447000','DD/MM/RR HH24:MI:SSXFF'),'34','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('11','10','add payload and upgrade','SQL','V10__add_payload_and_upgrade.sql','-440873924','MANAGER',to_timestamp('24/09/20 15:25:16,009427000','DD/MM/RR HH24:MI:SSXFF'),'29','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('1','0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',null,'MANAGER',to_timestamp('14/04/20 15:50:04,805970000','DD/MM/RR HH24:MI:SSXFF'),'0','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('2','1','init database','SQL','V1__init_database.sql','-1909929880','MANAGER',to_timestamp('14/04/20 15:50:05,920745000','DD/MM/RR HH24:MI:SSXFF'),'1039','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('3','2','fix history table','SQL','V2__fix_history_table.sql','1251449345','MANAGER',to_timestamp('14/04/20 15:50:06,200508000','DD/MM/RR HH24:MI:SSXFF'),'264','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('4','3','update version table','SQL','V3__update_version_table.sql','621824079','MANAGER',to_timestamp('14/04/20 15:50:06,257867000','DD/MM/RR HH24:MI:SSXFF'),'41','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('5','4','update dictionary table','SQL','V4__update_dictionary_table.sql','1028367596','MANAGER',to_timestamp('14/04/20 15:50:06,313390000','DD/MM/RR HH24:MI:SSXFF'),'40','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('6','5','add transformer table','SQL','V5__add_transformer_table.sql','-2117533992','MANAGER',to_timestamp('14/04/20 15:50:06,344643000','DD/MM/RR HH24:MI:SSXFF'),'16','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('8','7','update user table','SQL','V7__update_user_table.sql','-776875775','MANAGER',to_timestamp('24/04/20 16:12:23,468502000','DD/MM/RR HH24:MI:SSXFF'),'136','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('9','8','reset transformer cfg','SQL','V8__reset_transformer_cfg.sql','-1065023711','MANAGER',to_timestamp('30/04/20 17:52:25,487990000','DD/MM/RR HH24:MI:SSXFF'),'14','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('13','12','add anomaly mgmt','SQL','V12__add_anomaly_mgmt.sql','2143820926','MANAGER',to_timestamp('25/09/20 18:26:48,438904000','DD/MM/RR HH24:MI:SSXFF'),'107','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('7','6','add export tables','SQL','V6__add_export_tables.sql','-1539115125','MANAGER',to_timestamp('16/04/20 14:55:52,844569000','DD/MM/RR HH24:MI:SSXFF'),'189','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('15','14','update apply history table','SQL','V14__update_apply_history_table.sql','-1407312131','MANAGER',to_timestamp('29/10/20 10:15:46,567831000','DD/MM/RR HH24:MI:SSXFF'),'13','0');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('12','11','add basic indexes','SQL','V11__add_basic_indexes.sql','656657135','MANAGER',to_timestamp('24/09/20 15:49:35,170505000','DD/MM/RR HH24:MI:SSXFF'),'1395','1');
Insert into MANAGER."flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('14','13','update apply history table','SQL','V13__update_apply_history_table.sql','-1407312131','MANAGER',to_timestamp('29/10/20 10:15:46,526200000','DD/MM/RR HH24:MI:SSXFF'),'164','1');

CREATE UNIQUE INDEX "flyway_schema_history_pk" ON "flyway_schema_history" ("installed_rank") PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS ;
CREATE INDEX "flyway_schema_history_s_idx" ON "flyway_schema_history" ("success") PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS ;

  ALTER TABLE "flyway_schema_history" MODIFY ("installed_rank" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("description" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("type" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("script" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("installed_by" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("installed_on" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("execution_time" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("success" NOT NULL ENABLE);

  ALTER TABLE "flyway_schema_history" ADD CONSTRAINT "flyway_schema_history_pk" PRIMARY KEY ("installed_rank") USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS ENABLE;

COMMIT;