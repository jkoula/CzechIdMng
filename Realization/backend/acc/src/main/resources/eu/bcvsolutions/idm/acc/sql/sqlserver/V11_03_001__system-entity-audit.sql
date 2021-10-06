--
-- CzechIdM 11.3.0 Flyway script 
-- BCV solutions s.r.o.
--
-- System entity - add audit.

CREATE TABLE sys_system_entity_a (
	id binary(16) NOT NULL,
	rev numeric(19, 0) NOT NULL,
	revtype numeric(19, 0) NULL,
	created datetime2 NULL,
	created_m bit NULL,
	creator nvarchar(255) NULL,
	creator_m bit NULL,
	creator_id binary(16) NULL,
	creator_id_m bit NULL,
	modifier nvarchar(255) NULL,
	modifier_m bit NULL,
	modifier_id binary(16) NULL,
	modifier_id_m bit NULL,
	original_creator nvarchar(255) NULL,
	original_creator_m bit NULL,
	original_creator_id binary(16) NULL,
	original_creator_id_m bit NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_m bit NULL,
	original_modifier_id binary(16) NULL,
	original_modifier_id_m bit NULL,
	realm_id binary(16) NULL,
	realm_id_m bit NULL,
	entity_type nvarchar(255) NULL,
	entity_type_m bit NULL,
	uid nvarchar(1000) NULL,
	uid_m bit NULL,
	wish bit NULL,
	wish_m bit NULL,
	system_id binary(16) NULL,
	system_m bit NULL,
	CONSTRAINT sys_system_entity_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkgutv55u1gxo01kbg2tnspi7vr FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
