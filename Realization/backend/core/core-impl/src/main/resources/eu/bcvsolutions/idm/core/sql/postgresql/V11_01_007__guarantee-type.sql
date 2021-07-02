--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- This script fixing wrong V8_02_003__role-guarantee-by-role.sql where was used public schema.
-- This script replaced "V10_03_002__guarantee-type.sql" which was related on the wrong one.

-- Add role guarantee type attribute

-- type guarantee-identity
ALTER TABLE idm_role_guarantee ADD COLUMN IF NOT EXISTS guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN IF NOT EXISTS guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_a ADD COLUMN IF NOT EXISTS type_m boolean;
-- index
CREATE INDEX IF NOT EXISTS idx_idm_role_guarantee_type
  ON idm_role_guarantee
  USING btree
  (guarantee_type);

-- type guarantee-role
ALTER TABLE idm_role_guarantee_role ADD COLUMN IF NOT EXISTS guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN IF NOT EXISTS guarantee_type character varying(255);
ALTER TABLE idm_role_guarantee_role_a ADD COLUMN IF NOT EXISTS type_m boolean;
-- index
CREATE INDEX IF NOT EXISTS idx_idm_role_g_r_type
  ON idm_role_guarantee_role
  USING btree
  (guarantee_type);
