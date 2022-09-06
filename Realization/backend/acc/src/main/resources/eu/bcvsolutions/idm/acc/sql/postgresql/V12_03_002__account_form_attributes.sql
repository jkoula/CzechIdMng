--
-- CzechIdM 12.3.0 Flyway script 
-- BCV solutions s.r.o.
--
-- Account - add form attributes by schema.

CREATE TABLE acc_schema_form_attribute (
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
	default_value text NULL,
	attribute_id bytea NOT NULL,
	schema_id bytea NOT NULL,
	validation_max numeric(38,4) NULL,
	validation_min numeric(38,4) NULL,
	validation_regex varchar(2000) NULL,
	required bool NOT NULL,
	validation_unique bool NOT NULL,
    validation_message varchar(2000) NULL,
	CONSTRAINT acc_schema_form_attribute_pkey PRIMARY KEY (id),
	CONSTRAINT ux_acc_schema_form_att_r_a UNIQUE (attribute_id, schema_id)
);
CREATE INDEX idx_acc_schema_form_att_def ON acc_schema_form_attribute USING btree (attribute_id);
CREATE INDEX idx_acc_schema_form_acc ON acc_schema_form_attribute USING btree (schema_id);

CREATE TABLE acc_schema_form_attribute_a (
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
	transaction_id bytea NULL,
	transaction_id_m bool NULL,
	default_value text NULL,
	default_value_m bool NULL,
	attribute_id bytea NULL,
	form_attribute_m bool NULL,
	schema_id bytea NULL,
	schema_m bool NULL,
	validation_max numeric(38,4) NULL,
	max_m bool NULL,
	validation_min numeric(38,4) NULL,
	min_m bool NULL,
	validation_regex varchar(2000) NULL,
	regex_m bool NULL,
	required bool NULL,
	required_m bool NULL,
	validation_unique bool NULL,
    validation_message varchar(2000) NULL,
    validation_message_m bool NULL,
	unique_m bool NULL,
	CONSTRAINT acc_schema_form_attribute_a_pkey PRIMARY KEY (id, rev),
	CONSTRAINT fk3ubuashgqm0c7eomt2dxkemty FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

-- Account - add form values.

CREATE TABLE acc_account_form_value (
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
    boolean_value bool NULL,
    byte_value bytea NULL,
    confidential bool NOT NULL,
    date_value timestamp NULL,
    double_value numeric(38,4) NULL,
    long_value int8 NULL,
    persistent_type varchar(45) NOT NULL,
    seq int2 NULL,
    short_text_value varchar(2000) NULL,
    string_value text NULL,
    uuid_value bytea NULL,
    attribute_id bytea NOT NULL,
    owner_id bytea NOT NULL,
    CONSTRAINT acc_account_form_value_pkey PRIMARY KEY (id),
    CONSTRAINT acc_account_form_value_seq_check CHECK ((seq <= 99999))
);
CREATE INDEX idx_acc_account_form_a ON acc_account_form_value USING btree (owner_id);
CREATE INDEX idx_acc_account_form_a_def ON acc_account_form_value USING btree (attribute_id);
CREATE INDEX idx_acc_account_form_stxt ON acc_account_form_value USING btree (short_text_value);
CREATE INDEX idx_acc_account_form_uuid ON acc_account_form_value USING btree (uuid_value);


CREATE TABLE acc_account_form_value_a (
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
    transaction_id bytea NULL,
    transaction_id_m bool NULL,
    boolean_value bool NULL,
    boolean_value_m bool NULL,
    byte_value bytea NULL,
    byte_value_m bool NULL,
    confidential bool NULL,
    confidential_m bool NULL,
    date_value timestamp NULL,
    date_value_m bool NULL,
    double_value numeric(38,4) NULL,
    double_value_m bool NULL,
    long_value int8 NULL,
    long_value_m bool NULL,
    persistent_type varchar(45) NULL,
    persistent_type_m bool NULL,
    seq int2 NULL,
    seq_m bool NULL,
    short_text_value varchar(2000) NULL,
    short_text_value_m bool NULL,
    string_value text NULL,
    string_value_m bool NULL,
    uuid_value bytea NULL,
    uuid_value_m bool NULL,
    attribute_id bytea NULL,
    form_attribute_m bool NULL,
    owner_id bytea NULL,
    owner_m bool NULL,
    CONSTRAINT acc_account_form_value_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fkgxltx9j4twowoeoncaun9fho2 FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

-- Update account with mapping, form definition and external id

ALTER TABLE acc_account ADD COLUMN form_definition_id bytea;
ALTER TABLE acc_account ADD COLUMN external_id character varying(255);
ALTER TABLE acc_account_a ADD COLUMN form_definition_id bytea;
ALTER TABLE acc_account_a ADD COLUMN form_definition_m bool;
ALTER TABLE acc_account_a ADD COLUMN external_id character varying(255);
ALTER TABLE acc_account_a ADD COLUMN external_id_m bool;

CREATE INDEX idx_acc_account_form_def ON acc_account USING btree (form_definition_id);
