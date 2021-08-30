--
-- CzechIdM 11.2.0 Flyway script 
-- BCV solutions s.r.o.
--
-- System groups.

ALTER TABLE sys_role_system ADD create_acc_by_default bool NOT NULL DEFAULT TRUE;
ALTER TABLE sys_role_system_a ADD create_acc_by_default bool NULL;
ALTER TABLE sys_role_system_a ADD create_account_by_default_m bool NULL;

CREATE TABLE sys_system_group (
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
                                  code varchar(255) NOT NULL,
                                  description varchar(2000) NULL,
                                  disabled bool NOT NULL,
                                  "type" varchar(255) NOT NULL,
                                  CONSTRAINT sys_system_group_pkey PRIMARY KEY (id),
                                  CONSTRAINT ux_sys_system_group_code UNIQUE (code)
);

CREATE TABLE sys_system_group_a (
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
                                    code varchar(255) NULL,
                                    code_m bool NULL,
                                    description varchar(2000) NULL,
                                    description_m bool NULL,
                                    disabled bool NULL,
                                    disabled_m bool NULL,
                                    "type" varchar(255) NULL,
                                    type_m bool NULL,
                                    CONSTRAINT sys_system_group_a_pkey PRIMARY KEY (id, rev),
                                    CONSTRAINT fkffrqts6ysbbfov6gmrrxonhdm FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE sys_system_group_system (
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
                                         system_id bytea NOT NULL,
                                         system_group_id bytea NOT NULL,
                                         merge_attribute_id bytea NULL,
                                         CONSTRAINT sys_system_group_system_pkey PRIMARY KEY (id),
                                         CONSTRAINT ux_sys_group_sys_group_sys UNIQUE (system_id, system_group_id)
);
CREATE INDEX idx_sys_group_system_group_id ON sys_system_group_system USING btree (system_group_id);
CREATE INDEX idx_sys_group_system_id ON sys_system_group_system USING btree (system_id);

CREATE TABLE sys_system_group_system_a (
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
                                           system_id bytea NULL,
                                           system_m bool NULL,
                                           system_group_id bytea NULL,
                                           system_group_m bool NULL,
                                           merge_attribute_id bytea NULL,
                                           merge_attribute_m bool NULL,
                                           CONSTRAINT sys_system_group_system_a_pkey PRIMARY KEY (id, rev),
                                           CONSTRAINT fkqfkp48y1n6rd5wh0u001kpuex FOREIGN KEY (rev) REFERENCES idm_audit(id)
);



