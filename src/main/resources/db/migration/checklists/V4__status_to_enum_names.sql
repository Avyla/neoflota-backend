-- 1) Migrar datos existentes CamelCase -> ENUM_NAME
UPDATE checklist_instance SET status = 'PENDING'     WHERE status = 'Pending';
UPDATE checklist_instance SET status = 'IN_PROGRESS' WHERE status = 'InProgress';
UPDATE checklist_instance SET status = 'SUBMITTED'   WHERE status = 'Submitted';
UPDATE checklist_instance SET status = 'APPROVED'    WHERE status = 'Approved';
UPDATE checklist_instance SET status = 'REJECTED'    WHERE status = 'Rejected';
UPDATE checklist_instance SET status = 'EXPIRED'     WHERE status = 'Expired';

-- 2) Ajustar DEFAULT y constraint a los nombres del enum
ALTER TABLE checklist_instance
  ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE checklist_instance
  DROP CONSTRAINT IF EXISTS chk_instance_status;

ALTER TABLE checklist_instance
  ADD CONSTRAINT chk_instance_status
  CHECK (status IN ('PENDING','IN_PROGRESS','SUBMITTED','APPROVED','REJECTED','EXPIRED'));
