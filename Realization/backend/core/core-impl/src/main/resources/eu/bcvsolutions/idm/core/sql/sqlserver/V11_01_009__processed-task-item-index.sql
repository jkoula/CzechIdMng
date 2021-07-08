--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
-- 
-- add index to processed task items

IF NOT EXISTS (SELECT * FROM sys.indexes WHERE name='idm_processed_t_i_r_e' AND OBJECT_ID = OBJECT_ID('idm_processed_task_item'))
	CREATE INDEX idm_processed_t_i_r_e ON idm_processed_task_item (referenced_entity_id);
