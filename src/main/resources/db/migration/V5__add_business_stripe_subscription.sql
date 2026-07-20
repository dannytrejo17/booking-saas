ALTER TABLE business
    ADD COLUMN stripe_customer_id varchar(255) DEFAULT NULL,
    ADD COLUMN stripe_subscription_id varchar(255) DEFAULT NULL,
    ADD COLUMN subscription_status varchar(50) NOT NULL DEFAULT 'inactive';
