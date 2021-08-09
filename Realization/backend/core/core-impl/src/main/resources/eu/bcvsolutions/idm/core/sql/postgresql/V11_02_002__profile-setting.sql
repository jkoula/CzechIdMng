--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- profile setting

ALTER TABLE idm_profile ADD setting bytea NULL;
ALTER TABLE idm_profile_a ADD setting bytea NULL;
ALTER TABLE idm_profile_a ADD setting_m bool NULL;

