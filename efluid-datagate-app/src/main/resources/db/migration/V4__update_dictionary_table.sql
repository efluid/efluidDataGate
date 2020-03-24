--cannot change directly type from VARCHAR to CLOB need to convert type to LONG which can directly be converted to CLOB
ALTER TABLE DICTIONARY MODIFY SELECT_CLAUSE LONG;
ALTER TABLE DICTIONARY MODIFY SELECT_CLAUSE CLOB;