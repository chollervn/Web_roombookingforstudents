package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.RoomOrder;

public interface RoomOrderRepository extends JpaRepository<RoomOrder, Integer> {

	List<RoomOrder> findByUserId(Integer userId);

	RoomOrder findByOrderId(String orderId);

}
