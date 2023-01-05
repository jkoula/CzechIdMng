--
-- CzechIdM 13.0.0 Flyway script
-- BCV solutions s.r.o.
--
-- Provisioning archive account link
alter table sys_provisioning_archive
    add column
        account_id bytea;

create index if not exists idx_sys_p_o_arch_account
    on sys_provisioning_archive (account_id);

-- Provisioning operation account link
alter table sys_provisioning_operation
    add column
        account_id bytea;

create index if not exists idx_sys_p_o_account_id
    on sys_provisioning_operation (account_id);
