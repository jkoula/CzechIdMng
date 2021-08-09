--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- profile setting

ALTER TABLE idm_profile ADD setting image NULL;
ALTER TABLE idm_profile_a ADD setting image NULL;
ALTER TABLE idm_profile_a ADD setting_m bit NULL;

