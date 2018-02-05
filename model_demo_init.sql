-- Script de préparation de la BDD demo (schéma public)

DROP TABLE "TTABLEOTHER";
DROP TABLE "TMODELE";
DROP TABLE "TTYPEMATERIEL";
DROP TABLE "TCATEGORYMATERIEL";
DROP TABLE "TTABLEOTHERTEST2";
DROP TABLE "NOT_USED";
     
CREATE TABLE "TCATEGORYMATERIEL"
(
    "ID" bigint NOT NULL,
    "NOM" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "DETAIL" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    CONSTRAINT "TCATEGORYMATERIEL_pkey" PRIMARY KEY ("ID")
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "TCATEGORYMATERIEL" OWNER to "user";

CREATE TABLE "TTYPEMATERIEL"
(
    "ID" bigint NOT NULL,
    "TYPE" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "SERIE" character varying(256) COLLATE pg_catalog."default",
    "CATID" bigint NOT NULL,
    CONSTRAINT "TTYPEMATERIEL_pkey" PRIMARY KEY ("ID"),
    CONSTRAINT "CAT_TYPE" FOREIGN KEY ("CATID")
        REFERENCES "TCATEGORYMATERIEL" ("ID") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "TTYPEMATERIEL" OWNER to "user";

CREATE TABLE "TTABLEOTHER"
(
    "ID" bigint NOT NULL,
    "VALUE" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "COUNT" bigint,
    "WHEN" timestamp,
    "TYPEID" bigint NOT NULL,
    CONSTRAINT "TTABLEOTHER_pkey" PRIMARY KEY ("ID"),
    CONSTRAINT "TYPE_OTHE" FOREIGN KEY ("TYPEID")
        REFERENCES "TTYPEMATERIEL" ("ID") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "TTABLEOTHER" OWNER to "user";

CREATE TABLE "TTABLEOTHERTEST2"
(
    "ID" bigint NOT NULL,
    "VALUE1" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "VALUE2" character varying(2048) COLLATE pg_catalog."default" NOT NULL,
    "VALUE3" character varying(1024) COLLATE pg_catalog."default" NOT NULL,
    "WEIGHT" float,
    "LAST_UPDATED" timestamp,
    CONSTRAINT "TTABLEOTHERTEST2_pkey" PRIMARY KEY ("ID")
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "TTABLEOTHERTEST2" OWNER to "user";

CREATE TABLE "TMODELE"
(
    "ID" bigint NOT NULL,
    "CODE_SERIE" character varying(128) COLLATE pg_catalog."default" NOT NULL,
    "CREATE_DATE" timestamp,
    "FABRICANT" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "DESCRIPTION" character varying(1024) COLLATE pg_catalog."default" NOT NULL,
    "TYPEID" bigint NOT NULL,
    "ACTIF" boolean NOT NULL,
    CONSTRAINT "TMODELE_pkey" PRIMARY KEY ("ID"),
    CONSTRAINT "TYPE_MODE" FOREIGN KEY ("TYPEID")
        REFERENCES "TTYPEMATERIEL" ("ID") MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "TMODELE" OWNER to "user";

CREATE TABLE "NOT_USED"
(
    "ID" bigint NOT NULL,
    "VALUE" character varying(256) COLLATE pg_catalog."default" NOT NULL,
    "WEIGHT" float,
    "LAST_UPDATED" timestamp,
    CONSTRAINT "NOT_USED_pkey" PRIMARY KEY ("ID")
)
WITH (
    OIDS = FALSE
)
TABLESPACE pg_default;
ALTER TABLE "NOT_USED" OWNER to "user";


-- Pour l'instant des données très simples.

INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (1, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (2, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (3, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (4, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (5, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (6, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (7, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (8, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (9, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (10, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');
INSERT INTO "TTABLEOTHERTEST2"("ID", "VALUE1", "VALUE2", "VALUE3", "WEIGHT", "LAST_UPDATED") VALUES (11, 'Something', 'test', 'test2', 14.5, '2018-01-02 23:45:32');


