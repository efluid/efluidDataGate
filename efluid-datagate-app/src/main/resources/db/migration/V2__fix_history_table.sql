-- Query column in History table is not well specified in Oracle. Fix it to CLOB

ALTER TABLE APPLY_HISTORY MODIFY QUERY CLOB;