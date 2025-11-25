package com.ecom.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Room;
import com.ecom.model.RoomStatus;
import com.ecom.repository.RoomRepository;
import com.ecom.service.RoomService;

@Service
public class RoomServiceImpl implements RoomService {

	@Autowired
	private RoomRepository roomRepository;

	@Override
	public Room saveRoom(Room room) {
		return roomRepository.save(room);
	}

	@Override
	public List<Room> getAllRooms() {
		return roomRepository.findAll();
	}

	@Override
	public Page<Room> getAllRoomsPagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return roomRepository.findAll(pageable);
	}

	@Autowired
	private com.ecom.repository.CartRepository cartRepository;

	@Autowired
	private com.ecom.repository.DepositRepository depositRepository;

	@Autowired
	private com.ecom.repository.RoomOrderRepository roomOrderRepository;

	@Autowired
	private com.ecom.repository.RoomBookingRepository roomBookingRepository;

	@Autowired
	private com.ecom.repository.ReviewRepository reviewRepository;

	@Autowired
	private com.ecom.repository.ExpenseRepository expenseRepository;

	@Override
	public Boolean deleteRoom(Integer id) {
		Room room = roomRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(room)) {
			// Delete associated data first to avoid foreign key constraints
			cartRepository.deleteByRoomId(id);
			depositRepository.deleteByRoomId(id);
			roomOrderRepository.deleteByRoomId(id);
			roomBookingRepository.deleteByRoomId(id);
			reviewRepository.deleteByRoomId(id);
			expenseRepository.deleteByRoomId(id);

			roomRepository.delete(room);
			return true;
		}
		return false;
	}

	@Override
	public Room getRoomById(Integer id) {
		Room room = roomRepository.findById(id).orElse(null);
		return room;
	}

	@Override
	public Room updateRoom(Room room, MultipartFile image) {

		Room dbRoom = getRoomById(room.getId());

		String imageName = image.isEmpty() ? dbRoom.getImage() : image.getOriginalFilename();

		// Check for active bookings
		List<com.ecom.model.RoomBooking> bookings = roomBookingRepository.findByRoomId(room.getId());
		boolean hasActiveBookings = bookings.stream().anyMatch(b -> "ACTIVE".equalsIgnoreCase(b.getStatus()));

		if (!hasActiveBookings) {
			// Only allow updating structural details if room is empty
			dbRoom.setRoomName(room.getRoomName());
			dbRoom.setDescription(room.getDescription());
			dbRoom.setRoomType(room.getRoomType());
			dbRoom.setAddress(room.getAddress());
			dbRoom.setFullAddress(room.getFullAddress());
			dbRoom.setDistrict(room.getDistrict());
			dbRoom.setCity(room.getCity());
			dbRoom.setArea(room.getArea());
		}

		// Always allow updating these fields
		dbRoom.setMonthlyRent(room.getMonthlyRent());
		dbRoom.setMaxOccupants(room.getMaxOccupants());
		dbRoom.setImage(imageName);
		dbRoom.setHasWifi(room.getHasWifi());
		dbRoom.setHasAirConditioner(room.getHasAirConditioner());
		dbRoom.setHasParking(room.getHasParking());
		dbRoom.setHasElevator(room.getHasElevator());
		dbRoom.setAllowPets(room.getAllowPets());
		dbRoom.setDeposit(room.getDeposit());
		dbRoom.setElectricityCost(room.getElectricityCost());
		dbRoom.setWaterCost(room.getWaterCost());
		if (room.getContactPhone() != null) {
			dbRoom.setContactPhone(room.getContactPhone());
		}
		if (room.getContactName() != null) {
			dbRoom.setContactName(room.getContactName());
		}
		dbRoom.setIsAvailable(room.getIsAvailable());
		dbRoom.setIsActive(room.getIsActive());

		Room updateRoom = roomRepository.save(dbRoom);

		if (!ObjectUtils.isEmpty(updateRoom)) {

			if (!image.isEmpty()) {

				try {
					// Save to external uploads directory (not classpath)
					String uploadsDir = System.getProperty("user.dir") + File.separator + "uploads" + File.separator
							+ "room_img";
					Path uploadPath = Paths.get(uploadsDir);

					// Create directory if it doesn't exist
					if (!Files.exists(uploadPath)) {
						Files.createDirectories(uploadPath);
					}

					Path filePath = uploadPath.resolve(image.getOriginalFilename());
					Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			return room;
		}
		return null;
	}

	@Override
	public List<Room> getAllActiveRooms(String roomType) {
		List<Room> rooms = null;
		if (ObjectUtils.isEmpty(roomType)) {
			rooms = roomRepository.findByIsActiveTrue();
		} else {
			rooms = roomRepository.findByRoomType(roomType);
		}

		return rooms;
	}

	@Override
	public List<Room> searchRoom(String ch) {
		return roomRepository.findByRoomNameContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(ch, ch);
	}

	@Override
	public Page<Room> searchRoomPagination(Integer pageNo, Integer pageSize, String ch) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return roomRepository.findByRoomNameContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(ch, ch, pageable);
	}

	@Override
	public Page<Room> getAllActiveRoomPagination(Integer pageNo, Integer pageSize, String roomType, String sortBy,
			String city, Double minPrice, Double maxPrice) {

		Pageable pageable = createPageable(pageNo, pageSize, sortBy);
		Page<Room> pageRoom = null;

		// Apply filters
		if (ObjectUtils.isEmpty(roomType) && ObjectUtils.isEmpty(city) && minPrice == null && maxPrice == null) {
			// No filters
			pageRoom = roomRepository.findByIsActiveTrue(pageable);
		} else {
			// Use custom query with filters
			pageRoom = roomRepository.findByFilters(roomType, city, minPrice, maxPrice, pageable);
		}
		return pageRoom;
	}

	@Override
	public Page<Room> searchActiveRoomPagination(Integer pageNo, Integer pageSize, String roomType, String ch,
			String sortBy, String city, Double minPrice, Double maxPrice) {

		Page<Room> pageRoom = null;
		Pageable pageable = createPageable(pageNo, pageSize, sortBy);

		// Search with filters
		pageRoom = roomRepository.findBySearchAndFilters(ch, roomType, city, minPrice, maxPrice, pageable);

		return pageRoom;
	}

	// Helper method để tạo Pageable với sort
	private Pageable createPageable(Integer pageNo, Integer pageSize, String sortBy) {
		Sort sort = Sort.unsorted();

		if (sortBy != null && !sortBy.isEmpty()) {
			switch (sortBy) {
				case "priceLowToHigh":
					sort = Sort.by(Sort.Direction.ASC, "monthlyRent");
					break;
				case "priceHighToLow":
					sort = Sort.by(Sort.Direction.DESC, "monthlyRent");
					break;
				case "newest":
					sort = Sort.by(Sort.Direction.DESC, "id");
					break;
				default:
					sort = Sort.unsorted();
			}
		}

		return PageRequest.of(pageNo, pageSize, sort);
	}

	@Override
	public List<Room> getAvailableRooms() {
		return roomRepository.findByIsActiveTrueAndIsAvailableTrue();
	}

	@Override
	public List<Room> getRoomsByCity(String city) {
		return roomRepository.findByCity(city);
	}

	@Override
	public List<Room> getRoomsByPriceRange(Double minRent, Double maxRent) {
		return roomRepository.findByMonthlyRentBetween(minRent, maxRent);
	}

	@Override
	public List<Room> getRoomsByCityAndType(String city, String roomType) {
		return roomRepository.findByCityAndRoomType(city, roomType);
	}

	@Override
	public List<Room> getRoomsByOwnerId(Integer ownerId) {
		return roomRepository.findByOwnerId(ownerId);
	}

	@Override
	public Room updateRoomStatus(Integer roomId, String status) {
		Room room = roomRepository.findById(roomId).orElse(null);
		if (room != null) {
			// Convert String to RoomStatus enum - for backward compatibility
			try {
				RoomStatus roomStatus = RoomStatus.valueOf(status);
				room.setRoomStatus(roomStatus);
			} catch (IllegalArgumentException e) {
				// Fall back to AVAILABLE if invalid status
				room.setRoomStatus(RoomStatus.AVAILABLE);
			}
			return roomRepository.save(room);
		}
		return null;
	}

}
