package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Review;

public interface ReviewRepository extends JpaRepository<Review, Integer> {

	List<Review> findByRoomIdOrderByCreatedDateDesc(Integer roomId);

	List<Review> findByUserId(Integer userId);

	Long countByRoomId(Integer roomId);

	Boolean existsByRoomIdAndUserId(Integer roomId, Integer userId);

	@org.springframework.data.jpa.repository.Query("SELECT r FROM Review r WHERE r.roomId IN (SELECT rm.id FROM Room rm WHERE rm.ownerId = :ownerId) ORDER BY r.createdDate DESC")
	List<Review> findByOwnerId(@org.springframework.data.repository.query.Param("ownerId") Integer ownerId);

	@org.springframework.transaction.annotation.Transactional
	@org.springframework.data.jpa.repository.Modifying
	void deleteByRoomId(Integer roomId);
}
