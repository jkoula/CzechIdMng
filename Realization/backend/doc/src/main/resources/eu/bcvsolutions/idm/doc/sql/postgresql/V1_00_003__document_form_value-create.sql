-- Jirka Koula
--
-- creates document form value table
CREATE TABLE doc_document_form_value
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
  attribute_id bytea NOT NULL,
  owner_id bytea NOT NULL,
  CONSTRAINT doc_document_form_value_pkey PRIMARY KEY (id),
  CONSTRAINT doc_document_form_value_seq_check CHECK (seq <= 99999)
);

CREATE INDEX idx_doc_doc_form_a
  ON doc_document_form_value
  USING btree
  (owner_id);

CREATE INDEX idx_doc_doc_form_a_def
  ON doc_document_form_value
  USING btree
  (attribute_id);
