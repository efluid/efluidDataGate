-- Add content for versions

ALTER TABLE VERSIONS ADD DOMAINS_CONTENT CLOB;
ALTER TABLE VERSIONS ADD DICTIONARY_CONTENT CLOB;
ALTER TABLE VERSIONS ADD LINKS_CONTENT CLOB;
ALTER TABLE VERSIONS ADD MAPPINGS_CONTENT CLOB;