package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import com.ecom.model.Room;

public interface RoomService {

	public Room saveRoom(Room room);

	public List<Room> getAllRooms();

	public Boolean deleteRoom(Integer id);

	public Room getRoomById(Integer id);

	public Room updateRoom(Room room, MultipartFile file);

	public List<Room> getAllActiveRooms(String roomType);

	public List<Room> searchRoom(String ch);

	public Page<Room> getAllActiveRoomPagination(Integer pageNo, Integer pageSize, String roomType, String sortBy, String city, Double minPrice, Double maxPrice);

	public Page<Room> searchRoomPagination(Integer pageNo, Integer pageSize, String ch);

	public Page<Room> getAllRoomsPagination(Integer pageNo, Integer pageSize);

	public Page<Room> searchActiveRoomPagination(Integer pageNo, Integer pageSize, String roomType, String ch, String sortBy, String city, Double minPrice, Double maxPrice);

	public List<Room> getAvailableRooms();

	public List<Room> getRoomsByCity(String city);

	public List<Room> getRoomsByPriceRange(Double minRent, Double maxRent);

	public List<Room> getRoomsByCityAndType(String city, String roomType);

	public List<Room> getRoomsByOwnerId(Integer ownerId);

	public Room updateRoomStatus(Integer roomId, String status);

}
