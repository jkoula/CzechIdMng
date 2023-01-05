--
-- CzechIdM 13.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Account entity - add mapping column
alter table acc_account
    add column
        system_mapping_id bytea;

create index idx_acc_account_sys_mapping
    on acc_account (system_mapping_id);

-- Account entity audit - add mapping column
alter table acc_account_a
    add column
        system_mapping_id bytea;

alter table acc_account_a
    add column
        system_mapping_m boolean;