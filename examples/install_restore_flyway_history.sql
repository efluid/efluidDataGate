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
   ) SEGMENT CREATION DEFERRED
  PCTFREE 10 PCTUSED 40 INITRANS 1 MAXTRANS 255
 NOCOMPRESS LOGGING
  TABLESPACE "USERS";
  
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('1','0','<< Flyway Baseline >>','BASELINE','<< Flyway Baseline >>',null,(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:04,805970000','DD/MM/RR HH24:MI:SSXFF'),'0','1');
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('2','1','init database','SQL','V1__init_database.sql','1964508323',(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:05,920745000','DD/MM/RR HH24:MI:SSXFF'),'1039','1');
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('3','2','fix history table','SQL','V2__fix_history_table.sql','1251449345',(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:06,200508000','DD/MM/RR HH24:MI:SSXFF'),'264','1');
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('4','3','update version table','SQL','V3__update_version_table.sql','621824079',(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:06,257867000','DD/MM/RR HH24:MI:SSXFF'),'41','1');
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('5','4','update dictionary table','SQL','V4__update_dictionary_table.sql','1028367596',(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:06,313390000','DD/MM/RR HH24:MI:SSXFF'),'40','1');
Insert into "flyway_schema_history" ("installed_rank","version","description","type","script","checksum","installed_by","installed_on","execution_time","success") values ('6','5','add transformer table','SQL','V5__add_transformer_table.sql','-2117533992',(select sys_context( 'userenv', 'current_schema' ) from dual),to_timestamp('14/04/20 15:50:06,344643000','DD/MM/RR HH24:MI:SSXFF'),'16','1');

CREATE UNIQUE INDEX "flyway_schema_history_pk" ON "flyway_schema_history" ("installed_rank") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;
CREATE INDEX "flyway_schema_history_s_idx" ON "flyway_schema_history" ("success") 
  PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS" ;

  ALTER TABLE "flyway_schema_history" MODIFY ("installed_rank" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("description" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("type" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("script" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("installed_by" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("installed_on" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("execution_time" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" MODIFY ("success" NOT NULL ENABLE);
  ALTER TABLE "flyway_schema_history" ADD CONSTRAINT "flyway_schema_history_pk" PRIMARY KEY ("installed_rank")
  USING INDEX PCTFREE 10 INITRANS 2 MAXTRANS 255 COMPUTE STATISTICS 
  STORAGE(INITIAL 65536 NEXT 1048576 MINEXTENTS 1 MAXEXTENTS 2147483645
  PCTINCREASE 0 FREELISTS 1 FREELIST GROUPS 1
  BUFFER_POOL DEFAULT FLASH_CACHE DEFAULT CELL_FLASH_CACHE DEFAULT)
  TABLESPACE "USERS"  ENABLE;
