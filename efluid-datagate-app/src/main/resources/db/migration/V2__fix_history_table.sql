-- Query column in History table is not well specified in Oracle. Fix it to CLOB
ALTER TABLE APPLY_HISTORY
ADD QUERY_NEW CLOB;
UPDATE APPLY_HISTORY SET QUERY_NEW = QUERY;
ALTER TABLE APPLY_HISTORY DROP COLUMN QUERY;
ALTER TABLE APPLY_HISTORY RENAME COLUMN QUERY_NEW TO QUERY;
