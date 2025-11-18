package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

	List<Review> findByRoomIdOrderByCreatedDateDesc(Integer roomId);

	List<Review> findByUserId(Integer userId);

	Long countByRoomId(Integer roomId);

	Boolean existsByRoomIdAndUserId(Integer roomId, Integer userId);
}

