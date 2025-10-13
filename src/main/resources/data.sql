-- Spring Boot data initialization file
-- Dữ liệu mẫu cho hệ thống cho thuê phòng trọ sinh viên miền Bắc

-- Xóa dữ liệu cũ nếu có để tránh conflict
DELETE FROM user_dtls WHERE email = 'admin@phongtrosv.com';

-- Thêm tài khoản admin mặc định (username: admin@phongtrosv.com, password: admin123)
INSERT INTO user_dtls (name, email, mobile_number, password, role, is_enable, account_non_locked, failed_attempt, profile_image) VALUES
('Admin PhongTroSV', 'admin@phongtrosv.com', '0900000000', '$2a$12$LQv3c1yqBaTVfC5f0So49.L5Bc/87Y0C.v2zKgMXfBNuOrPDMIuam', 'ROLE_ADMIN', true, true, 0, 'default.jpg');

-- Thêm các loại phòng trọ (RoomType)
INSERT INTO room_type (name, image_name, is_active)
SELECT 'Phòng trọ đơn', 'single-room.jpg', true
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE name = 'Phòng trọ đơn');

INSERT INTO room_type (name, image_name, is_active)
SELECT 'Phòng trọ đôi', 'double-room.jpg', true
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE name = 'Phòng trọ đôi');

INSERT INTO room_type (name, image_name, is_active)
SELECT 'Căn hộ mini', 'mini-apartment.jpg', true
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE name = 'Căn hộ mini');

INSERT INTO room_type (name, image_name, is_active)
SELECT 'Phòng có gác lửng', 'loft-room.jpg', true
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE name = 'Phòng có gác lửng');

INSERT INTO room_type (name, image_name, is_active)
SELECT 'Phòng VIP', 'vip-room.jpg', true
WHERE NOT EXISTS (SELECT 1 FROM room_type WHERE name = 'Phòng VIP');

-- Thêm các phòng trọ ở miền Bắc (chỉ thêm nếu chưa có dữ liệu)
INSERT INTO room (room_name, description, room_type, monthly_rent, address, full_address, district, city, area, max_occupants, image, has_wifi, has_air_conditioner, has_parking, has_elevator, allow_pets, deposit, electricity_cost, water_cost, contact_phone, contact_name, is_available, is_active)
SELECT * FROM (VALUES
-- Hà Nội
('Phòng trọ gần ĐH Bách Khoa', 'Phòng trọ sạch sẽ, an ninh tốt, gần trường ĐH Bách Khoa Hà Nội. Có đầy đủ tiện ích: wifi, máy lạnh, tủ lạnh nhỏ.', 'Phòng trọ đơn', 2500000, 'Ngõ 123 Tôn Đức Thắng', 'Ngõ 123 Tôn Đức Thắng, Đống Đa, Hà Nội', 'Đống Đa', 'Hà Nội', 18.5, 1, 'room1.jpg', true, true, false, false, false, 5000000, 3500, 25000, '0987654321', 'Chị Hoa', true, true),

('Căn hộ mini Láng Hạ', 'Căn hộ mini 1 phòng ngủ, 1 phòng khách, bếp riêng. Khu vực an ninh, gần các trường đại học.', 'Căn hộ mini', 4500000, 'Số 45 Láng Hạ', 'Số 45 Láng Hạ, Đống Đa, Hà Nội', 'Đống Đa', 'Hà Nội', 35.0, 2, 'room2.jpg', true, true, true, true, false, 9000000, 3500, 25000, '0912345678', 'Anh Minh', true, true),

('Phòng đôi gần ĐH Quốc Gia', 'Phòng rộng 25m2, có 2 giường, tủ quần áo lớn. Gần ĐH Quốc Gia Việt Nam và các trường trong khu vực.', 'Phòng trọ đôi', 3200000, 'Ngõ 56 Xuân Thủy', 'Ngõ 56 Xuân Thủy, Cầu Giấy, Hà Nội', 'Cầu Giấy', 'Hà Nội', 25.0, 2, 'room3.jpg', true, true, false, false, true, 6400000, 3500, 20000, '0934567890', 'Chú Tuấn', true, true),

('Phòng có gác lửng Duy Tân', 'Phòng 2 tầng với gác lửng làm phòng ngủ, tầng dưới làm phòng học. Rất tiện cho sinh viên.', 'Phòng có gác lửng', 3800000, 'Ngõ 234 Duy Tân', 'Ngõ 234 Duy Tân, Cầu Giấy, Hà Nội', 'Cầu Giấy', 'Hà Nội', 28.0, 1, 'room4.jpg', true, true, true, false, false, 7600000, 3500, 20000, '0945678901', 'Bác Hùng', true, true),

('Phòng VIP Bạch Mai', 'Phòng VIP đầy đủ tiện nghi, có ban công, gần bệnh viện Bạch Mai và các trường y khoa.', 'Phòng VIP', 5500000, 'Ngõ 78 Bạch Mai', 'Ngõ 78 Bạch Mai, Hai Bà Trưng, Hà Nội', 'Hai Bà Trưng', 'Hà Nội', 32.0, 1, 'room5.jpg', true, true, true, true, false, 11000000, 3500, 25000, '0956789012', 'Cô Mai', true, true),

('Phòng trọ Giải Phóng', 'Phòng trọ giá rẻ cho sinh viên, gần các trường đại học khu vực Giải Phóng.', 'Phòng trọ đơn', 2200000, 'Ngõ 345 Giải Phóng', 'Ngõ 345 Giải Phóng, Hai Bà Trưng, Hà Nội', 'Hai Bà Trưng', 'Hà Nội', 16.0, 1, 'room6.jpg', true, false, false, false, true, 4400000, 3000, 20000, '0967890123', 'Chị Lan', true, true),

('Căn hộ mini Mỹ Đình', 'Căn hộ mini hiện đại ở khu Mỹ Đình, gần các trường đại học FPT, Đại học Thương mại.', 'Căn hộ mini', 5000000, 'Tòa CT1 Mỹ Đình', 'Tòa CT1 Mỹ Đình, Nam Từ Liêm, Hà Nội', 'Nam Từ Liêm', 'Hà Nội', 40.0, 2, 'room7.jpg', true, true, true, true, true, 10000000, 3500, 30000, '0978901234', 'Anh Đức', true, true),

-- Hải Phòng
('Phòng trọ gần ĐH Hàng Hải', 'Phòng trọ gần Đại học Hàng hải Việt Nam, môi trường thoáng mát, gần biển.', 'Phòng trọ đơn', 2000000, 'Ngõ 12 Lạch Tray', 'Ngõ 12 Lạch Tray, Ngô Quyền, Hải Phòng', 'Ngô Quyền', 'Hải Phòng', 20.0, 1, 'room8.jpg', true, true, false, false, false, 4000000, 3000, 20000, '0989012345', 'Chị Hương', true, true),

('Phòng đôi Lê Chân', 'Phòng rộng rãi cho 2 sinh viên, gần trung tâm thành phố Hải Phòng.', 'Phòng trọ đôi', 2800000, 'Ngõ 89 Tô Hiệu', 'Ngõ 89 Tô Hiệu, Lê Chân, Hải Phòng', 'Lê Chân', 'Hải Phòng', 24.0, 2, 'room9.jpg', true, false, true, false, true, 5600000, 3000, 18000, '0990123456', 'Anh Hải', true, true),

-- Thái Nguyên
('Phòng trọ ĐH Thái Nguyên', 'Phòng trọ dành cho sinh viên ĐH Thái Nguyên, giá cả phải chăng, môi trường học tập tốt.', 'Phòng trọ đơn', 1800000, 'Ngõ 67 Hoàng Văn Thụ', 'Ngõ 67 Hoàng Văn Thụ, Tân Thịnh, Thái Nguyên', 'Tân Thịnh', 'Thái Nguyên', 18.0, 1, 'room10.jpg', true, false, false, false, false, 3600000, 2800, 15000, '0901234567', 'Bác Sơn', true, true),

-- Bắc Ninh
('Căn hộ mini Từ Sơn', 'Căn hộ mini tại Từ Sơn, Bắc Ninh. Gần các khu công nghiệp và trường đại học.', 'Căn hộ mini', 3500000, 'Khu TT Từ Sơn', 'Khu Trung tâm Từ Sơn, Từ Sơn, Bắc Ninh', 'Từ Sơn', 'Bắc Ninh', 30.0, 2, 'room11.jpg', true, true, true, false, false, 7000000, 3200, 20000, '0912345670', 'Chị Nga', true, true),

-- Vĩnh Phúc
('Phòng VIP Vĩnh Yên', 'Phòng VIP với đầy đủ tiện nghi tại trung tâm Vĩnh Yên, gần trường đại học.', 'Phòng VIP', 4200000, 'Ngõ 123 Mê Linh', 'Ngõ 123 Mê Linh, Vĩnh Yên, Vĩnh Phúc', 'Vĩnh Yên', 'Vĩnh Phúc', 28.0, 1, 'room12.jpg', true, true, true, false, true, 8400000, 3200, 22000, '0923456781', 'Anh Phúc', true, true)
) AS new_rooms(room_name, description, room_type, monthly_rent, address, full_address, district, city, area, max_occupants, image, has_wifi, has_air_conditioner, has_parking, has_elevator, allow_pets, deposit, electricity_cost, water_cost, contact_phone, contact_name, is_available, is_active)
WHERE NOT EXISTS (SELECT 1 FROM room LIMIT 1);
