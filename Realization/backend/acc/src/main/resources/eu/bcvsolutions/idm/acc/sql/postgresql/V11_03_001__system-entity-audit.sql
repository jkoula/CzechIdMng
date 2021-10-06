--
-- CzechIdM 11.3.0 Flyway script 
-- BCV solutions s.r.o.
--
-- System entity - add audit.

CREATE TABLE sys_system_entity_a (
	id bytea NOT NULL,
	rev int8 NOT NULL,
	revtype int2 NULL,
	created timestamp NULL,
	created_m bool NULL,
	creator varchar(255) NULL,
	creator_m bool NULL,
	creator_id bytea NULL,
	creator_id_m bool NULL,
	modifier varchar(255) NULL,
	modifier_m bool NULL,
	modifier_id bytea NULL,
	modifier_id_m bool NULL,
	original_creator varchar(255) NULL,
	original_creator_m bool NULL,
	original_creator_id bytea NULL,
	original_creator_id_m bool NULL,
	original_modifier varchar(255) NULL,
	original_modifier_m bool NULL,
	original_modifier_id bytea NULL,
	original_modifier_id_m bool NULL,
	realm_id bytea NULL,
	realm_id_m bool NULL,
	entity_type varchar(255) NULL,
	entity_type_m bool NULL,
	uid varchar(1000) NULL,
	uid_m bool NULL,
	wish bool NULL,
	wish_m bool NULL,
	system_id bytea NULL,
	system_m bool NULL,
	CONSTRAINT sys_system_entity_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fkgutv55u1gxo01kbg2tnspi7vr FOREIGN KEY (rev) REFERENCES idm_audit(id)
);
