--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
-- 
-- add index to processed task items

CREATE INDEX IF NOT EXISTS idm_processed_t_i_r_e ON idm_processed_task_item (referenced_entity_id);
