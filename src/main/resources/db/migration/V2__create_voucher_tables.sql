-- V2: Create vouchers and game_records tables for Duck Race minigame

-- Create vouchers table
CREATE TABLE IF NOT EXISTS vouchers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(20) UNIQUE NOT NULL,
    discount_percent INT NOT NULL CHECK (discount_percent BETWEEN 5 AND 20),
    user_id INT NOT NULL,
    is_used BOOLEAN DEFAULT FALSE NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expiry_date TIMESTAMP NOT NULL,
    used_date TIMESTAMP NULL,
    FOREIGN KEY (user_id) REFERENCES user_dtls(id) ON DELETE CASCADE,
    INDEX idx_user_vouchers (user_id, is_used, expiry_date),
    INDEX idx_voucher_code (code)
);

-- Create game_records table
CREATE TABLE IF NOT EXISTS game_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    game_type VARCHAR(50) DEFAULT 'DUCK_RACE' NOT NULL,
    result VARCHAR(10) NOT NULL CHECK (result IN ('WIN', 'LOSE')),
    selected_duck INT NOT NULL CHECK (selected_duck BETWEEN 1 AND 6),
    winning_duck INT NOT NULL CHECK (winning_duck BETWEEN 1 AND 6),
    voucher_id BIGINT NULL,
    played_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES user_dtls(id) ON DELETE CASCADE,
    FOREIGN KEY (voucher_id) REFERENCES vouchers(id) ON DELETE SET NULL,
    INDEX idx_user_games (user_id, played_date),
    INDEX idx_game_date (played_date)
);
