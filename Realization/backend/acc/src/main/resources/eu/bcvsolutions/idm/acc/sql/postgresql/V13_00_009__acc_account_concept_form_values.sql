--
-- CzechIdM Certificates 2.3.0 Flyway script
-- BCV solutions s.r.o.
--

----- TABLE acc_account_concept_request_form_value -----
CREATE TABLE acc_account_concept_request_form_value
(
    id bytea NOT NULL,
    created timestamp without time zone NOT NULL,
    creator character varying(255) NOT NULL,
    creator_id bytea,
    modified timestamp without time zone,
    modifier character varying(255),
    modifier_id bytea,
    original_creator character varying(255),
    original_creator_id bytea,
    original_modifier character varying(255),
    original_modifier_id bytea,
    realm_id bytea,
    transaction_id bytea,
    boolean_value boolean,
    byte_value bytea,
    confidential boolean NOT NULL,
    date_value timestamp without time zone,
    double_value numeric(38,4),
    long_value bigint,
    persistent_type character varying(45) NOT NULL,
    seq smallint,
    string_value text,
    short_text_value character varying(2000),
    attribute_id bytea NOT NULL,
    owner_id bytea NOT NULL,
    uuid_value bytea,
    CONSTRAINT acc_account_concept_request_form_value_pkey PRIMARY KEY (id),
    CONSTRAINT acc_account_concept_request_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_acc_account_concept_request_form
    ON acc_account_concept_request_form_value
    USING btree
    (owner_id);

CREATE INDEX idx_acc_account_concept_request_form_def
    ON acc_account_concept_request_form_value
    USING btree
    (attribute_id);

CREATE INDEX idx_acc_account_concept_request_form_stxt
    ON acc_account_concept_request_form_value
    USING btree
    (short_text_value);

CREATE INDEX idx_acc_account_concept_request_form_uuid
    ON acc_account_concept_request_form_value
    USING btree
    (uuid_value);


----- TABLE acc_account_concept_request_form_value_a -----
CREATE TABLE acc_account_concept_request_form_value_a
(
    id bytea NOT NULL,
    rev bigint NOT NULL,
    revtype smallint,
    created timestamp without time zone,
    created_m boolean,
    creator character varying(255),
    creator_m boolean,
    creator_id bytea,
    creator_id_m boolean,
    modified timestamp without time zone,
    modified_m boolean,
    modifier character varying(255),
    modifier_m boolean,
    modifier_id bytea,
    modifier_id_m boolean,
    original_creator character varying(255),
    original_creator_m boolean,
    original_creator_id bytea,
    original_creator_id_m boolean,
    original_modifier character varying(255),
    original_modifier_m boolean,
    original_modifier_id bytea,
    original_modifier_id_m boolean,
    realm_id bytea,
    realm_id_m boolean,
    transaction_id bytea,
    transaction_id_m boolean,
    boolean_value boolean,
    boolean_value_m boolean,
    byte_value bytea,
    byte_value_m boolean,
    confidential boolean,
    confidential_m boolean,
    date_value timestamp without time zone,
    date_value_m boolean,
    double_value numeric(38,4),
    double_value_m boolean,
    long_value bigint,
    long_value_m boolean,
    persistent_type character varying(45),
    persistent_type_m boolean,
    seq smallint,
    seq_m boolean,
    string_value text,
    string_value_m boolean,
    short_text_value character varying(2000),
    short_text_value_m boolean,
    attribute_id bytea,
    form_attribute_m boolean,
    owner_id bytea,
    owner_m boolean,
    uuid_value bytea,
    uuid_value_m boolean,
    CONSTRAINT acc_account_concept_request_form_value_a_pkey PRIMARY KEY (id, rev),
    CONSTRAINT fk_acc_account_concept_request_form_value_a FOREIGN KEY (rev)
        REFERENCES idm_audit (id) MATCH SIMPLE
        ON UPDATE NO ACTION ON DELETE NO ACTION
);
