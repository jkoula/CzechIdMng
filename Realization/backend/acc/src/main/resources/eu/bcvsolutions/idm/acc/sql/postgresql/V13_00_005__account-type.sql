--
-- CzechIdM 13.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Account entity - remove account type column
alter table acc_account
    DROP COLUMN account_type;

-- Account entity audit - remove account type column
alter table acc_account_a
    DROP COLUMN account_type;

alter table acc_account_a
    DROP COLUMN account_type_m;

-- System mapping entity - add account type column
alter table sys_system_mapping
    add column
        account_type varchar(255);

-- set default value
update sys_system_mapping set account_type = 'PERSONAL' where account_type is null;

-- System mapping entity audit - add account type column
alter table sys_system_mapping_a
    add column
        account_type varchar(255);

alter table sys_system_mapping_a
    add column
        account_type_m boolean;