package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.RoomOrder;

public interface RoomOrderRepository extends JpaRepository<RoomOrder, Integer> {

	List<RoomOrder> findByUserId(Integer userId);

	RoomOrder findByOrderId(String orderId);

	@Query("SELECT ro FROM RoomOrder ro WHERE ro.room.ownerId = :ownerId")
	List<RoomOrder> findByRoomOwnerId(@Param("ownerId") Integer ownerId);

}
