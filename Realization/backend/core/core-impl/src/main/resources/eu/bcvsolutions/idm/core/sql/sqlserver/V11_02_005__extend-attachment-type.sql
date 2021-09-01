--
-- CzechIdM 11 Flyway script 
-- BCV solutions s.r.o.
--
-- extend attachment type length

ALTER TABLE idm_attachment ALTER COLUMN attachment_type nvarchar(255) NULL;

CREATE INDEX idx_idm_attachment_a_type ON idm_attachment (attachment_type);