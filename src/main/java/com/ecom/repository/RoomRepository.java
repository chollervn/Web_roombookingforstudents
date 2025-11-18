package com.ecom.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

	// Custom query for filtering with multiple conditions
	@Query("SELECT r FROM Room r WHERE r.isActive = true " +
		   "AND (:roomType IS NULL OR :roomType = '' OR r.roomType = :roomType) " +
		   "AND (:city IS NULL OR :city = '' OR r.city = :city) " +
		   "AND (:minPrice IS NULL OR r.monthlyRent >= :minPrice) " +
		   "AND (:maxPrice IS NULL OR r.monthlyRent <= :maxPrice)")
	Page<Room> findByFilters(@Param("roomType") String roomType,
							 @Param("city") String city,
							 @Param("minPrice") Double minPrice,
							 @Param("maxPrice") Double maxPrice,
							 Pageable pageable);

	// Custom query for search with filters
	@Query("SELECT r FROM Room r WHERE r.isActive = true " +
		   "AND (r.roomName LIKE %:keyword% OR r.roomType LIKE %:keyword% OR r.address LIKE %:keyword%) " +
		   "AND (:roomType IS NULL OR :roomType = '' OR r.roomType = :roomType) " +
		   "AND (:city IS NULL OR :city = '' OR r.city = :city) " +
		   "AND (:minPrice IS NULL OR r.monthlyRent >= :minPrice) " +
		   "AND (:maxPrice IS NULL OR r.monthlyRent <= :maxPrice)")
	Page<Room> findBySearchAndFilters(@Param("keyword") String keyword,
									   @Param("roomType") String roomType,
									   @Param("city") String city,
									   @Param("minPrice") Double minPrice,
									   @Param("maxPrice") Double maxPrice,
									   Pageable pageable);
}
