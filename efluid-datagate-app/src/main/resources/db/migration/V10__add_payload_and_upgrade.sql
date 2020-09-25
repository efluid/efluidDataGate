-- Add "previous" column in index
ALTER TABLE "INDEXES"
    ADD "PREVIOUS" CLOB;

-- Add support for automatic upgrade of data
CREATE TABLE "UPGRADES"
(
    "NAME"     VARCHAR2(255 CHAR) NOT NULL ENABLE,
    "IDX_NUM"  NUMBER(19, 0)      NOT NULL ENABLE,
    "RUN_TIME" TIMESTAMP(6)       NOT NULL ENABLE,
    PRIMARY KEY ("NAME") ENABLE
);