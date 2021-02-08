--
-- CzechIdM 11.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Test resource extension for tests with flyway enabled

ALTER TABLE test_resource ADD TITLE_BEFORE nvarchar(MAX);
ALTER TABLE test_resource ADD TITLE_AFTER nvarchar(MAX);
ALTER TABLE test_resource ADD PERSONAL_NUMBER nvarchar(MAX);