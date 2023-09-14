-- Jirka Koula
--
-- creates document table
CREATE TABLE doc_document
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
  uuid character varying(1000) NOT NULL,
  "type" character varying(45) NOT NULL,
  "number" character varying(1000) NOT NULL,
  first_name character varying(255) NOT NULL,
  last_name character varying(255) NOT NULL,
  state character varying(45) NOT NULL,
  identity_id bytea NOT NULL,
  CONSTRAINT doc_document_pkey PRIMARY KEY (id)
);

-- max 1 VALID document of given type for each Identity
CREATE UNIQUE INDEX ux_doc_document_valid_state
ON doc_document (identity_id, "type", state)
WHERE state = 'VALID';
