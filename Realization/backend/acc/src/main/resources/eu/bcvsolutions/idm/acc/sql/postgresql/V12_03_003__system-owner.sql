--
-- CzechIdM 12.3.0 Flyway script
-- BCV solutions s.r.o.
--
-- System owner
create table idm_system_owner
(
    id                   bytea        not null
        primary key,
    created              timestamp    not null,
    creator              varchar(255) not null,
    creator_id           bytea,
    modified             timestamp,
    modifier             varchar(255),
    modifier_id          bytea,
    original_creator     varchar(255),
    original_creator_id  bytea,
    original_modifier    varchar(255),
    original_modifier_id bytea,
    realm_id             bytea,
    transaction_id       bytea,
    external_id          varchar(255),
    owner_id             bytea        not null,
    system_id            bytea        not null
);

create index idx_idm_system_owner_system
    on idm_system_owner (system_id);

create index idx_idm_system_owner_gnt
    on idm_system_owner (owner_id);

create index idx_idm_system_owner_ext_id
    on idm_system_owner (external_id);

-- System owner audit
create table idm_system_owner_a
(
    id                     bytea  not null,
    rev                    bigint not null
        constraint fkdyjrou7sr2b75gbvdj1whr1uv
            references idm_audit,
    revtype                smallint,
    created                timestamp,
    created_m              boolean,
    creator                varchar(255),
    creator_m              boolean,
    creator_id             bytea,
    creator_id_m           boolean,
    modifier               varchar(255),
    modifier_m             boolean,
    modifier_id            bytea,
    modifier_id_m          boolean,
    original_creator       varchar(255),
    original_creator_m     boolean,
    original_creator_id    bytea,
    original_creator_id_m  boolean,
    original_modifier      varchar(255),
    original_modifier_m    boolean,
    original_modifier_id   bytea,
    original_modifier_id_m boolean,
    realm_id               bytea,
    realm_id_m             boolean,
    external_id            varchar(255),
    external_id_m          boolean,
    owner_id               bytea,
    owner_m                boolean,
    system_id              bytea,
    system_m               boolean,
    primary key (id, rev)
);

-- System owner by role
create table idm_system_owner_role
(
    id                   bytea        not null
        primary key,
    created              timestamp    not null,
    creator              varchar(255) not null,
    creator_id           bytea,
    modified             timestamp,
    modifier             varchar(255),
    modifier_id          bytea,
    original_creator     varchar(255),
    original_creator_id  bytea,
    original_modifier    varchar(255),
    original_modifier_id bytea,
    realm_id             bytea,
    transaction_id       bytea,
    external_id          varchar(255),
    owner_role_id        bytea        not null,
    system_id            bytea        not null
);

create index idx_idm_system_owner_role_system
    on idm_system_owner_role (system_id);

create index idx_idm_system_owner_role_role
    on idm_system_owner_role (owner_role_id);

create index idx_idm_system_owner_role_ext_id
    on idm_system_owner_role (external_id);

-- System owner by role audit
create table idm_system_owner_role_a
(
    id                     bytea  not null,
    rev                    bigint not null
        constraint fk7bhpd0tsh7ht7hflahrms4pjj
            references idm_audit,
    revtype                smallint,
    created                timestamp,
    created_m              boolean,
    creator                varchar(255),
    creator_m              boolean,
    creator_id             bytea,
    creator_id_m           boolean,
    modifier               varchar(255),
    modifier_m             boolean,
    modifier_id            bytea,
    modifier_id_m          boolean,
    original_creator       varchar(255),
    original_creator_m     boolean,
    original_creator_id    bytea,
    original_creator_id_m  boolean,
    original_modifier      varchar(255),
    original_modifier_m    boolean,
    original_modifier_id   bytea,
    original_modifier_id_m boolean,
    realm_id               bytea,
    realm_id_m             boolean,
    external_id            varchar(255),
    external_id_m          boolean,
    owner_role_id          bytea,
    owner_role_m           boolean,
    system_id              bytea,
    system_m               boolean,
    primary key (id, rev)
);