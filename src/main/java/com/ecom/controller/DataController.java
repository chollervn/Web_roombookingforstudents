package com.ecom.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ecom.model.Room;
import com.ecom.model.RoomType;
import com.ecom.service.RoomService;
import com.ecom.service.RoomTypeService;

@RestController
@RequestMapping("/api/data")
public class DataController {

    @Autowired
    private RoomTypeService roomTypeService;

    @Autowired
    private RoomService roomService;

    @GetMapping("/init")
    public String initializeData() {
        try {
            // Tạo các loại phòng nếu chưa có
            if (roomTypeService.getAllRoomType().isEmpty()) {
                createRoomTypes();
            }

            // Tạo các phòng mẫu nếu chưa có
            if (roomService.getAllRooms().isEmpty()) {
                createSampleRooms();
            }

            return "Dữ liệu mẫu đã được khởi tạo thành công!";
        } catch (Exception e) {
            return "Lỗi khi khởi tạo dữ liệu: " + e.getMessage();
        }
    }

    private void createRoomTypes() {
        String[] roomTypeNames = {
            "Phòng trọ đơn", "Phòng trọ đôi", "Căn hộ mini",
            "Phòng có gác lửng", "Phòng VIP"
        };

        for (String name : roomTypeNames) {
            if (!roomTypeService.existRoomType(name)) {
                RoomType roomType = new RoomType();
                roomType.setName(name);
                roomType.setImageName("default.jpg");
                roomType.setIsActive(true);
                roomTypeService.saveRoomType(roomType);
            }
        }
    }

    private void createSampleRooms() {
        // Phòng trọ mẫu ở Hà Nội
        createRoom("Phòng trọ gần ĐH Bách Khoa",
                  "Phòng trọ sạch sẽ, an ninh tốt, gần trường ĐH Bách Khoa Hà Nội. Có đầy đủ tiện ích: wifi, máy lạnh, tủ lạnh nhỏ.",
                  "Phòng trọ đơn", 2500000.0, "Ngõ 123 Tôn Đức Thắng",
                  "Ngõ 123 Tôn Đức Thắng, Đống Đa, Hà Nội", "Đống Đa", "Hà Nội",
                  18.5, 1, "0987654321", "Chị Hoa");

        createRoom("Căn hộ mini Láng Hạ",
                  "Căn hộ mini 1 phòng ngủ, 1 phòng khách, bếp riêng. Khu vực an ninh, gần các trường đại học.",
                  "Căn hộ mini", 4500000.0, "Số 45 Láng Hạ",
                  "Số 45 Láng Hạ, Đống Đa, Hà Nội", "Đống Đa", "Hà Nội",
                  35.0, 2, "0912345678", "Anh Minh");

        createRoom("Phòng đôi gần ĐH Quốc Gia",
                  "Phòng rộng 25m2, có 2 giường, tủ quần áo lớn. Gần ĐH Quốc Gia Việt Nam và các trường trong khu vực.",
                  "Phòng trọ đôi", 3200000.0, "Ngõ 56 Xuân Thủy",
                  "Ngõ 56 Xuân Thủy, Cầu Giấy, Hà Nội", "Cầu Giấy", "Hà Nội",
                  25.0, 2, "0934567890", "Chú Tuấn");

        // Phòng trọ ở Hải Phòng
        createRoom("Phòng trọ gần ĐH Hàng Hải",
                  "Phòng trọ gần Đại học Hàng hải Việt Nam, môi trường thoáng mát, gần biển.",
                  "Phòng trọ đơn", 2000000.0, "Ngõ 12 Lạch Tray",
                  "Ngõ 12 Lạch Tray, Ngô Quyền, Hải Phòng", "Ngô Quyền", "Hải Phòng",
                  20.0, 1, "0989012345", "Chị Hương");

        // Phòng trọ ở Thái Nguyên
        createRoom("Phòng trọ ĐH Thái Nguyên",
                  "Phòng trọ dành cho sinh viên ĐH Thái Nguyên, giá cả phải chăng, môi trường học tập tốt.",
                  "Phòng trọ đôn", 1800000.0, "Ngõ 67 Hoàng Văn Thụ",
                  "Ngõ 67 Hoàng Văn Thụ, Tân Thịnh, Thái Nguyên", "Tân Thịnh", "Thái Nguyên",
                  18.0, 1, "0901234567", "Bác Sơn");
    }

    private void createRoom(String roomName, String description, String roomType, Double monthlyRent,
                           String address, String fullAddress, String district, String city,
                           Double area, Integer maxOccupants, String contactPhone, String contactName) {
        Room room = new Room();
        room.setRoomName(roomName);
        room.setDescription(description);
        room.setRoomType(roomType);
        room.setMonthlyRent(monthlyRent);
        room.setAddress(address);
        room.setFullAddress(fullAddress);
        room.setDistrict(district);
        room.setCity(city);
        room.setArea(area);
        room.setMaxOccupants(maxOccupants);
        room.setImage("default-room.jpg");
        room.setHasWifi(true);
        room.setHasAirConditioner(true);
        room.setHasParking(false);
        room.setHasElevator(false);
        room.setAllowPets(false);
        room.setDeposit(monthlyRent * 2);
        room.setElectricityCost(3500.0);
        room.setWaterCost(25000.0);
        room.setContactPhone(contactPhone);
        room.setContactName(contactName);
        room.setIsAvailable(true);
        room.setIsActive(true);

        roomService.saveRoom(room);
    }

    @GetMapping("/rooms/count")
    public String getRoomsCount() {
        List<Room> rooms = roomService.getAllRooms();
        List<RoomType> roomTypes = roomTypeService.getAllRoomType();
        return String.format("Hiện có %d phòng trọ và %d loại phòng trong hệ thống",
                           rooms.size(), roomTypes.size());
    }
}
