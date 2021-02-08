--
-- CzechIdM 11.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Test resource extension for tests with flyway enabled

ALTER TABLE test_resource ADD title_before varchar(255) NULL;
ALTER TABLE test_resource ADD title_after varchar(255) NULL;
ALTER TABLE test_resource ADD personal_number varchar(255) NULL;
