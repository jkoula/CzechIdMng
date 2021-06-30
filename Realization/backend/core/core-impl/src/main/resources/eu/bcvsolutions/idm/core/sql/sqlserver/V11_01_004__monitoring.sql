--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- monitoring agenda

CREATE TABLE idm_monitoring (
	id binary(16) NOT NULL,
	created datetime2(7) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(7) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	description nvarchar(2000) NULL,
	disabled bit NOT NULL,
	evaluator_properties image NULL,
	evaluator_type nvarchar(255) NOT NULL,
	instance_id nvarchar(255) NOT NULL,
	seq smallint NULL,
	check_period numeric(19,0) NULL,
	execute_date datetime2(7) NULL,
	CONSTRAINT idm_monitoring_check_period_check CHECK (((check_period >= 0) AND (check_period <= 9223372036854775807))),
	CONSTRAINT idm_monitoring_pkey PRIMARY KEY (id),
	CONSTRAINT idm_monitoring_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_idm_monitoring_e_type ON idm_monitoring (evaluator_type);
CREATE INDEX idx_idm_monitoring_inst ON idm_monitoring (instance_id);

CREATE TABLE idm_monitoring_a (
	id binary(16) NOT NULL,
	rev numeric(19,0) NOT NULL,
	revtype smallint NULL,
	created datetime2(7) NULL,
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
	check_period numeric(19,0) NULL,
	check_period_m bit NULL,
	description nvarchar(2000) NULL,
	description_m bit NULL,
	disabled bit NULL,
	disabled_m bit NULL,
	evaluator_properties image NULL,
	evaluator_properties_m bit NULL,
	evaluator_type nvarchar(255) NULL,
	evaluator_type_m bit NULL,
	instance_id nvarchar(255) NULL,
	instance_id_m bit NULL,
	seq smallint NULL,
	seq_m bit NULL,
	execute_date datetime2(7) NULL,
	execute_date_m bit NULL,
	CONSTRAINT idm_monitoring_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fknmgthoafyrvfvo8c6m2uvug1a FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_monitoring_result (
	id binary(16) NOT NULL,
	created datetime2(7) NOT NULL,
	creator nvarchar(255) NOT NULL,
	creator_id binary(16) NULL,
	modified datetime2(7) NULL,
	modifier nvarchar(255) NULL,
	modifier_id binary(16) NULL,
	original_creator nvarchar(255) NULL,
	original_creator_id binary(16) NULL,
	original_modifier nvarchar(255) NULL,
	original_modifier_id binary(16) NULL,
	realm_id binary(16) NULL,
	transaction_id binary(16) NULL,
	evaluator_properties image NULL,
	evaluator_type nvarchar(255) NOT NULL,
	instance_id nvarchar(255) NOT NULL,
	"level" nvarchar(45) NULL,
	monitoring_id binary(16) NOT NULL,
	processed_order smallint NULL,
	result_cause nvarchar(MAX) NULL,
	result_code nvarchar(255) NULL,
	result_model image NULL,
	result_state nvarchar(45) NULL,
	value nvarchar(MAX) NULL,
	owner_id binary(16) NULL,
	owner_type nvarchar(255) NULL,
	monitoring_ended datetime2(7) NULL,
	monitoring_started datetime2(7) NULL,
	CONSTRAINT idm_monitoring_result_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_monitoring_mon_id ON idm_monitoring_result (monitoring_id);
CREATE INDEX idx_idm_monitoring_r_e_type ON idm_monitoring_result (evaluator_type);
CREATE INDEX idx_idm_monitoring_r_inst ON idm_monitoring_result (instance_id);

