-- Add support for anomalies
CREATE TABLE "ANOMALIES"
(
    "ID"           NUMBER(19, 0)      NOT NULL ENABLE,
    "CODE"         VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "MESSAGE"      CLOB,
    "CONTEXT_TYPE" VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "CONTEXT_NAME" VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "DETECT_TIME"  TIMESTAMP(6)       NOT NULL ENABLE,
    PRIMARY KEY ("ID") ENABLE
);