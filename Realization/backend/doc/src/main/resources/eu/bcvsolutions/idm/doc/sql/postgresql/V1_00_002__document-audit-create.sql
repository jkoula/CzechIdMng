-- Jirka Koula
--
-- creates document audit table
CREATE TABLE doc_document
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
  uuid character varying(1000),
  uuid_m boolean,
  "type" character varying(45) NOT NULL,
  type_m boolean,
  "number" character varying(1000) NOT NULL,
  number_m boolean,
  first_name character varying(255) NOT NULL,
  first_name_m boolean,
  last_name character varying(255) NOT NULL,
  last_name_m boolean,
  state character varying(45) NOT NULL,
  state_m boolean,
  identity_id bytea NOT NULL,
  identity_id_m boolean,
  CONSTRAINT doc_document_a_pkey PRIMARY KEY (id, rev),
  CONSTRAINT fk_doc_document_audit FOREIGN KEY (rev)
      REFERENCES idm_audit (id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
);
