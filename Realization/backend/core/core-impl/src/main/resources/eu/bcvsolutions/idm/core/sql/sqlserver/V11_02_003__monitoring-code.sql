--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- Add code to monitoring evaluator
--
ALTER TABLE idm_monitoring ADD code nvarchar(255);
ALTER TABLE idm_monitoring_a ADD code nvarchar(255);
ALTER TABLE idm_monitoring_a ADD code_m bit
GO
UPDATE idm_monitoring set code = LOWER(CONVERT(UNIQUEIDENTIFIER, id)); -- code = id by default
-- add not null
ALTER TABLE idm_monitoring ALTER COLUMN code nvarchar(255) NOT NULL;
--
CREATE UNIQUE INDEX ux_idm_monitoring_code ON idm_monitoring (code);


