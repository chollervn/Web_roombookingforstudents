-- Migration script to add new columns for room rental status management
-- Run this script on your PostgreSQL database

-- 1. Add room_status column (enum stored as VARCHAR)
ALTER TABLE room 
ADD COLUMN IF NOT EXISTS room_status VARCHAR(20) DEFAULT 'AVAILABLE';

-- 2. Add room_leader_id column (reference to user who signed contract)
ALTER TABLE room 
ADD COLUMN IF NOT EXISTS room_leader_id INT;

-- 3. Add foreign key constraint for room_leader_id
ALTER TABLE room 
ADD CONSTRAINT IF NOT EXISTS fk_room_leader 
FOREIGN KEY (room_leader_id) REFERENCES user_dtls(id);

-- 4. Migrate existing data: Set status based on current state
-- If room is not available, set to OCCUPIED, otherwise AVAILABLE
UPDATE room 
SET room_status = CASE 
    WHEN is_available = false THEN 'OCCUPIED'
    ELSE 'AVAILABLE'
END
WHERE room_status IS NULL;

-- 5. Create index for better performance
CREATE INDEX IF NOT EXISTS idx_room_status ON room(room_status);
CREATE INDEX IF NOT EXISTS idx_room_leader ON room(room_leader_id);

-- Verify migration
SELECT COUNT(*) as total_rooms, room_status, COUNT(*) as count_per_status
FROM room 
GROUP BY room_status;
