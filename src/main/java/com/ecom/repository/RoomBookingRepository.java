package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.RoomBooking;

public interface RoomBookingRepository extends JpaRepository<RoomBooking, Integer> {

	List<RoomBooking> findByUserId(Integer userId);

	List<RoomBooking> findByRoomId(Integer roomId);

	@Query("SELECT rb FROM RoomBooking rb WHERE rb.room.ownerId = :ownerId")
	List<RoomBooking> findByRoomOwnerId(@Param("ownerId") Integer ownerId);

	RoomBooking findByUserIdAndRoomId(Integer userId, Integer roomId);

	List<RoomBooking> findByStatus(String status);

	@org.springframework.transaction.annotation.Transactional
	@org.springframework.data.jpa.repository.Modifying
	void deleteByRoomId(Integer roomId);
}
