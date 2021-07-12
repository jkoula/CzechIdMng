--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- last result flag
ALTER TABLE idm_monitoring_result ADD COLUMN last_result boolean NOT NULL DEFAULT false;