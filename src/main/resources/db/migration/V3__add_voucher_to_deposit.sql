-- Add voucher support to deposit table
ALTER TABLE deposit
ADD COLUMN voucher_id BIGINT,
ADD COLUMN discount_amount DOUBLE,
ADD COLUMN original_amount DOUBLE;

-- Add foreign key constraint
ALTER TABLE deposit
ADD CONSTRAINT fk_deposit_voucher
FOREIGN KEY (voucher_id) REFERENCES vouchers(id);

