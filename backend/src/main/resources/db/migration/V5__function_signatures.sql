ALTER TABLE problems ADD COLUMN function_name VARCHAR(100);
ALTER TABLE problems ADD COLUMN params JSONB DEFAULT '[]';
ALTER TABLE problems ADD COLUMN return_type VARCHAR(50);
