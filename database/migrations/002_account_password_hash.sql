-- Apply once before starting the application with BCrypt password hashing.
ALTER TABLE ACCOUNT MODIFY COLUMN PASSWORD VARCHAR(255) NOT NULL;
