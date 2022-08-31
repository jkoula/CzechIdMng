create table acc_account_concept_role_request
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
    direct_concept_id    bytea,
    direct_role_id       bytea,
    log                  text,
    operation            varchar(255),
    role_composition_id  bytea,
    role_system_id       bytea,
    state                varchar(255) not null,
    result_cause         text,
    result_code          varchar(255),
    result_model         bytea,
    result_state         varchar(45),
    valid_from           date,
    valid_till           date,
    wf_process_id        varchar(255),
    automatic_role_id    bytea,
    role_id              bytea,
    request_role_id      bytea        not null,
    account_id           bytea,
    identity_role_id     bytea
);

create index idx_acc_conc_role_account_id
    on acc_account_concept_role_request (account_id);

create index idx_acc_conc_role_request
    on acc_account_concept_role_request (request_role_id);

create index idx_acc_conc_role_role
    on acc_account_concept_role_request (role_id);

create table acc_account_concept_role_request_a
(
    id                     bytea  not null,
    rev                    bigint not null
        constraint fk64goqj9649bibqoojfwnyu534
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
    direct_concept_id      bytea,
    direct_concept_m       boolean,
    direct_role_id         bytea,
    direct_role_m          boolean,
    operation              varchar(255),
    operation_m            boolean,
    role_composition_id    bytea,
    role_composition_m     boolean,
    role_system_id         bytea,
    role_system_m          boolean,
    state                  varchar(255),
    state_m                boolean,
    valid_from             date,
    valid_from_m           boolean,
    valid_till             date,
    valid_till_m           boolean,
    wf_process_id          varchar(255),
    wf_process_id_m        boolean,
    automatic_role_id      bytea,
    automatic_role_m       boolean,
    role_id                bytea,
    role_m                 boolean,
    request_role_id        bytea,
    role_request_m         boolean,
    account_id             bytea,
    acc_account_m          boolean,
    identity_role_id       bytea,
    account_role_m         boolean,
    primary key (id, rev)
);

create table acc_account_role_assignment
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
    role_system_id       bytea,
    valid_from           date,
    valid_till           date,
    external_id          varchar(255),
    automatic_role_id    bytea,
    direct_role_id       bytea,
    role_id              bytea        not null,
    role_composition_id  bytea,
    account_id           bytea        not null
);

alter table acc_account_role_assignment
    owner to idmadmin;

create index idx_acc_account_role_assign_ident_a
    on acc_account_role_assignment (account_id);

create index idx_acc_account_role_assign_role
    on acc_account_role_assignment (role_id);

create index idx_acc_account_role_assign_aut_r
    on acc_account_role_assignment (automatic_role_id);

create index idx_acc_account_role_assign_ext_id
    on acc_account_role_assignment (external_id);

create index idx_acc_account_role_assign_d_r_id
    on acc_account_role_assignment (direct_role_id);

create index idx_acc_account_role_assign_comp_id
    on acc_account_role_assignment (role_composition_id);

create table acc_account_role_assignment_a
(
    id                     bytea  not null,
    rev                    bigint not null
        constraint fk74eo9d9bdbo6d72lir1k44jri
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
    role_system_id         bytea,
    role_system_m          boolean,
    valid_from             date,
    valid_from_m           boolean,
    valid_till             date,
    valid_till_m           boolean,
    external_id            varchar(255),
    external_id_m          boolean,
    automatic_role_id      bytea,
    automatic_role_m       boolean,
    direct_role_id         bytea,
    direct_role_m          boolean,
    role_id                bytea,
    role_m                 boolean,
    role_composition_id    bytea,
    role_composition_m     boolean,
    account_id             bytea,
    account_m              boolean,
    primary key (id, rev)
);

