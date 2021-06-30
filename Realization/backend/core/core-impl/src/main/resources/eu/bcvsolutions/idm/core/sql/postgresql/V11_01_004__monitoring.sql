--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- monitoring agenda

CREATE TABLE idm_monitoring (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	description varchar(2000) NULL,
	disabled bool NOT NULL,
	evaluator_properties bytea NULL,
	evaluator_type varchar(255) NOT NULL,
	instance_id varchar(255) NOT NULL,
	seq int2 NULL,
	check_period int8 NULL,
	execute_date timestamp NULL,
	CONSTRAINT idm_monitoring_check_period_check CHECK (((check_period >= 0) AND (check_period <= '9223372036854775807'::bigint))),
	CONSTRAINT idm_monitoring_pkey PRIMARY KEY (id),
	CONSTRAINT idm_monitoring_seq_check CHECK (((seq >= '-32768'::integer) AND (seq <= 32767)))
);
CREATE INDEX idx_idm_monitoring_e_type ON idm_monitoring USING btree (evaluator_type);
CREATE INDEX idx_idm_monitoring_inst ON idm_monitoring USING btree (instance_id);

CREATE TABLE idm_monitoring_a (
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
	check_period int8 NULL,
	check_period_m bool NULL,
	description varchar(2000) NULL,
	description_m bool NULL,
	disabled bool NULL,
	disabled_m bool NULL,
	evaluator_properties bytea NULL,
	evaluator_properties_m bool NULL,
	evaluator_type varchar(255) NULL,
	evaluator_type_m bool NULL,
	instance_id varchar(255) NULL,
	instance_id_m bool NULL,
	seq int2 NULL,
	seq_m bool NULL,
	execute_date timestamp NULL,
	execute_date_m bool NULL,
	CONSTRAINT idm_monitoring_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fknmgthoafyrvfvo8c6m2uvug1a FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE idm_monitoring_result (
	id bytea NOT NULL,
	created timestamp NOT NULL,
	creator varchar(255) NOT NULL,
	creator_id bytea NULL,
	modified timestamp NULL,
	modifier varchar(255) NULL,
	modifier_id bytea NULL,
	original_creator varchar(255) NULL,
	original_creator_id bytea NULL,
	original_modifier varchar(255) NULL,
	original_modifier_id bytea NULL,
	realm_id bytea NULL,
	transaction_id bytea NULL,
	evaluator_properties bytea NULL,
	evaluator_type varchar(255) NOT NULL,
	instance_id varchar(255) NOT NULL,
	"level" varchar(45) NULL,
	monitoring_id bytea NOT NULL,
	processed_order int2 NULL,
	result_cause text NULL,
	result_code varchar(255) NULL,
	result_model bytea NULL,
	result_state varchar(45) NULL,
	value text NULL,
	owner_id bytea NULL,
	owner_type varchar(255) NULL,
	monitoring_ended timestamp NULL,
	monitoring_started timestamp NULL,
	CONSTRAINT idm_monitoring_result_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_idm_monitoring_mon_id ON idm_monitoring_result USING btree (monitoring_id);
CREATE INDEX idx_idm_monitoring_r_e_type ON idm_monitoring_result USING btree (evaluator_type);
CREATE INDEX idx_idm_monitoring_r_inst ON idm_monitoring_result USING btree (instance_id);

