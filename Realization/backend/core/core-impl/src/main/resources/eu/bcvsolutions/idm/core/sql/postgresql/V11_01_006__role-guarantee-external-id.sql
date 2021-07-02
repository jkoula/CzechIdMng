--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- This script fixing wrong V8_02_003__role-guarantee-by-role.sql where was used public schema.
-- This script replaced "V9_00_005__role-guarantee-external-id.sql" which was related on the wrong one.

-- External id for core entities, which could be synchronized from external source

ALTER TABLE idm_role_guarantee ADD COLUMN IF NOT EXISTS external_id character varying(255);
CREATE INDEX IF NOT EXISTS idx_idm_role_guarantee_ext_id
  ON idm_role_guarantee
  USING btree
  (external_id);
ALTER TABLE idm_role_guarantee_a ADD COLUMN IF NOT EXISTS external_id character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN IF NOT EXISTS external_id_m boolean;


ALTER TABLE idm_role_guarantee_role ADD COLUMN IF NOT EXISTS external_id character varying(255);
CREATE INDEX IF NOT EXISTS idx_idm_role_g_r_ext_id
  ON idm_role_guarantee_role
  USING btree
  (external_id);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN IF NOT EXISTS external_id character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN IF NOT EXISTS external_id_m boolean;
