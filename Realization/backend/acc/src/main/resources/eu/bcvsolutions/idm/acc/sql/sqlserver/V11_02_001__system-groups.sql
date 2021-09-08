--
-- CzechIdM 11.2.0 Flyway script 
-- BCV solutions s.r.o.
--
-- System groups.

ALTER TABLE sys_role_system ADD create_acc_by_default bit NOT NULL DEFAULT 1;
ALTER TABLE sys_role_system_a ADD create_acc_by_default bit NULL;
ALTER TABLE sys_role_system_a ADD create_account_by_default_m bit NULL;

CREATE TABLE sys_system_group (
                                  id binary(16) NOT NULL,
                                  created datetime2 NOT NULL,
                                  creator nvarchar(255) NOT NULL,
                                  creator_id binary(16) NULL,
                                  modified datetime2 NULL,
                                  modifier nvarchar(255) NULL,
                                  modifier_id binary(16) NULL,
                                  original_creator nvarchar(255) NULL,
                                  original_creator_id binary(16) NULL,
                                  original_modifier nvarchar(255) NULL,
                                  original_modifier_id binary(16) NULL,
                                  realm_id binary(16) NULL,
                                  transaction_id binary(16) NULL,
                                  code nvarchar(255) NOT NULL,
                                  description nvarchar(2000) NULL,
                                  disabled bit NOT NULL,
                                  "type" nvarchar(255) NOT NULL,
                                  CONSTRAINT sys_system_group_pkey PRIMARY KEY (id),
                                  CONSTRAINT ux_sys_system_group_code UNIQUE (code)
);

CREATE TABLE sys_system_group_a (
                                    id binary(16) NOT NULL,
                                    rev numeric(19, 0) NOT NULL,
                                    revtype numeric(19,0) NULL,
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
                                    code nvarchar(255) NULL,
                                    code_m bit NULL,
                                    description nvarchar(2000) NULL,
                                    description_m bit NULL,
                                    disabled bit NULL,
                                    disabled_m bit NULL,
                                    "type" nvarchar(255) NULL,
                                    type_m bit NULL,
                                    CONSTRAINT sys_system_group_a_pkey PRIMARY KEY (id, rev),
                                    CONSTRAINT fkffrqts6ysbbfov6gmrrxonhdm FOREIGN KEY (rev) REFERENCES idm_audit(id)
);

CREATE TABLE sys_system_group_system (
                                         id binary(16) NOT NULL,
                                         created datetime2 NOT NULL,
                                         creator nvarchar(255) NOT NULL,
                                         creator_id binary(16) NULL,
                                         modified datetime2 NULL,
                                         modifier nvarchar(255) NULL,
                                         modifier_id binary(16) NULL,
                                         original_creator nvarchar(255) NULL,
                                         original_creator_id binary(16) NULL,
                                         original_modifier nvarchar(255) NULL,
                                         original_modifier_id binary(16) NULL,
                                         realm_id binary(16) NULL,
                                         transaction_id binary(16) NULL,
                                         system_id binary(16) NOT NULL,
                                         system_group_id binary(16) NOT NULL,
                                         merge_attribute_id binary(16) NULL,
                                         CONSTRAINT sys_system_group_system_pkey PRIMARY KEY (id),
                                         CONSTRAINT ux_sys_group_sys_group_sys UNIQUE (system_id, system_group_id)
);
CREATE INDEX idx_sys_group_system_group_id ON sys_system_group_system (system_group_id);
CREATE INDEX idx_sys_group_system_id ON sys_system_group_system (system_id);

CREATE TABLE sys_system_group_system_a (
                                           id binary(16) NOT NULL,
                                           rev numeric(19, 0) NOT NULL,
                                           revtype numeric(19,0) NULL,
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
                                           system_id binary(16) NULL,
                                           system_m bit NULL,
                                           system_group_id binary(16) NULL,
                                           system_group_m bit NULL,
                                           merge_attribute_id binary(16) NULL,
                                           merge_attribute_m bit NULL,
                                           CONSTRAINT sys_system_group_system_a_pkey PRIMARY KEY (id, rev),
                                           CONSTRAINT fkqfkp48y1n6rd5wh0u001kpuex FOREIGN KEY (rev) REFERENCES idm_audit(id)
);



