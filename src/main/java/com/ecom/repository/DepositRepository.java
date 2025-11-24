package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.Deposit;

public interface DepositRepository extends JpaRepository<Deposit, Integer> {

	List<Deposit> findByUserId(Integer userId);

	List<Deposit> findByRoomId(Integer roomId);

	@Query("SELECT d FROM Deposit d WHERE d.room.ownerId = :ownerId")
	List<Deposit> findByRoomOwnerId(@Param("ownerId") Integer ownerId);

	List<Deposit> findByStatus(String status);

	@org.springframework.transaction.annotation.Transactional
	@org.springframework.data.jpa.repository.Modifying
	void deleteByRoomId(Integer roomId);

	Long countByUserIdAndStatusInAndIsNotificationSeenFalse(Integer userId, List<String> statuses);

	List<Deposit> findByUserIdAndStatusInAndIsNotificationSeenFalse(Integer userId, List<String> statuses);
}
