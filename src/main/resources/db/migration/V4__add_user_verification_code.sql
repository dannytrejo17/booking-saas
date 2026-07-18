ALTER TABLE `user`
    ADD COLUMN verification_code varchar(6) DEFAULT NULL,
    ADD COLUMN verification_code_expires_at datetime(6) DEFAULT NULL;
