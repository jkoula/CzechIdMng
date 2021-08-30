--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- Add relation on a role-system (for cross-domains).

ALTER TABLE idm_concept_role_request ADD role_system_id binary(16) NULL;
ALTER TABLE idm_concept_role_request_a ADD role_system_id binary(16) NULL;
ALTER TABLE idm_concept_role_request_a ADD role_system_m bit NULL;

ALTER TABLE idm_identity_role ADD role_system_id binary(16) NULL;
ALTER TABLE idm_identity_role_a ADD role_system_id binary(16) NULL;
ALTER TABLE idm_identity_role_a ADD role_system_m bit NULL;




