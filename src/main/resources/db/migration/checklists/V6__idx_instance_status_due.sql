CREATE INDEX IF NOT EXISTS idx_checklist_instance_status_due
    ON checklist_instance (status, due_at);