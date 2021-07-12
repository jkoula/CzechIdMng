--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- last result flag
ALTER TABLE idm_monitoring_result ADD last_result bit NOT NULL DEFAULT 0;