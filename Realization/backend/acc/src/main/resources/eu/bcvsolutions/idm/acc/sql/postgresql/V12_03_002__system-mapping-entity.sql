--
-- CzechIdM 12.3.0 Flyway script
-- BCV solutions s.r.o.
--
-- System mapping entity - add mapping column
alter table sys_system_mapping
    add column
        connected_system_mapping_id bytea;

create index idx_sys_s_mapping_c_s_mapping
    on sys_system_mapping (connected_system_mapping_id);

-- System mapping entity audit - add mapping column
alter table sys_system_mapping_a
    add column
        connected_system_mapping_id bytea;

alter table sys_system_mapping_a
    add column
        connected_system_mapping_id_m boolean;