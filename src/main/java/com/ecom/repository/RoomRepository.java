package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Room;

public interface RoomRepository extends JpaRepository<Room, Integer> {

	List<Room> findByIsActiveTrue();

	Page<Room> findByIsActiveTrue(Pageable pageable);

	List<Room> findByIsActiveTrueAndIsAvailableTrue();

	Page<Room> findByIsActiveTrueAndIsAvailableTrue(Pageable pageable);

	List<Room> findByRoomType(String roomType);

	Page<Room> findByRoomType(Pageable pageable, String roomType);

	List<Room> findByCity(String city);

	Page<Room> findByCity(Pageable pageable, String city);

	List<Room> findByDistrict(String district);

	Page<Room> findByDistrict(Pageable pageable, String district);

	List<Room> findByRoomNameContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(String ch, String ch2);

	Page<Room> findByRoomNameContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(String ch, String ch2, Pageable pageable);

	Page<Room> findByisActiveTrueAndRoomNameContainingIgnoreCaseOrRoomTypeContainingIgnoreCase(String ch, String ch2, Pageable pageable);

	List<Room> findByMonthlyRentBetween(Double minRent, Double maxRent);

	Page<Room> findByMonthlyRentBetween(Double minRent, Double maxRent, Pageable pageable);

	List<Room> findByCityAndRoomType(String city, String roomType);

	List<Room> findByCityAndMonthlyRentBetween(String city, Double minRent, Double maxRent);

	List<Room> findByOwnerId(Integer ownerId);
}
