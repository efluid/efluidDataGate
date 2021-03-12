-- ADD COLUMN REVERT_SOURCE_UUID IN COMMITS TABLE FOR REVERT LINK
declare
    column_exists exception;
    pragma exception_init (column_exists , -01430);
begin
    execute immediate 'ALTER TABLE "COMMITS" ADD "REVERT_SOURCE_UUID" VARCHAR2(255)';
exception when column_exists then null;
end;
