--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- Add code to monitoring evaluator
--
ALTER TABLE idm_monitoring ADD COLUMN code varchar(255);
ALTER TABLE idm_monitoring_a ADD COLUMN code varchar(255);
ALTER TABLE idm_monitoring_a ADD COLUMN code_m boolean;
-- code = id by default
UPDATE idm_monitoring set code = encode(id, 'hex')::uuid;
-- add not null
ALTER TABLE idm_monitoring ALTER COLUMN code SET NOT NULL;
--
CREATE UNIQUE INDEX ux_idm_monitoring_code 
  ON idm_monitoring 
  USING btree
  (code);


