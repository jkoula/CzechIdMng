--
-- CzechIdM 13 Flyway script
-- BCV solutions s.r.o.
--
-- Add applicant_type to role request

ALTER TABLE idm_role_request ADD applicant_type varchar(255) NULL;
ALTER TABLE idm_role_request_a ADD applicant_type varchar(255) NULL;
ALTER TABLE idm_role_request_a ADD applicant_type_m varchar(255) NULL;






