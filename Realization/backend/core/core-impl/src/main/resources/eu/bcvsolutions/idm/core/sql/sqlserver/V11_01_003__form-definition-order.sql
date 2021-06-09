--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- addorder for form definitions

ALTER TABLE idm_form_definition ADD seq smallint NULL;
ALTER TABLE idm_form_definition_a ADD seq smallint NULL;
ALTER TABLE idm_form_definition_a ADD seq_m bit NULL;

