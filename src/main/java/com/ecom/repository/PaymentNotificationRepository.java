package com.ecom.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.PaymentNotification;

public interface PaymentNotificationRepository extends JpaRepository<PaymentNotification, Integer> {

	// Lấy tất cả thông báo của một payment
	List<PaymentNotification> findByPayment_IdOrderBySentDateDesc(Integer paymentId);

	// Lấy thông báo theo loại
	List<PaymentNotification> findByType(String type);

	// Lấy thông báo theo trạng thái
	List<PaymentNotification> findByStatus(String status);

	// Lấy thông báo của owner (thông qua payment -> booking -> room -> ownerId)
	@Query("SELECT pn FROM PaymentNotification pn WHERE pn.payment.roomBooking.room.ownerId = :ownerId ORDER BY pn.sentDate DESC")
	List<PaymentNotification> findByOwnerId(@Param("ownerId") Integer ownerId);

	// Lấy thông báo của user (thông qua payment -> booking -> user -> id)
	List<PaymentNotification> findByPayment_RoomBooking_User_IdOrderBySentDateDesc(Integer userId);

	// Lấy thông báo trong khoảng thời gian
	@Query("SELECT pn FROM PaymentNotification pn WHERE pn.sentDate BETWEEN :startDate AND :endDate ORDER BY pn.sentDate DESC")
	List<PaymentNotification> findBySentDateBetween(@Param("startDate") LocalDateTime startDate,
			@Param("endDate") LocalDateTime endDate);

	// Đếm số thông báo đã gửi cho một payment
	Long countByPayment_Id(Integer paymentId);

	// Đếm số thông báo chưa đọc của user
	// Đếm số thông báo chưa đọc của user
	@Query("SELECT COUNT(pn) FROM PaymentNotification pn WHERE pn.payment.roomBooking.user.id = :userId AND pn.status != 'READ'")
	Long countUnreadByUserId(@Param("userId") Integer userId);

	// Lấy thông báo mới nhất của user (dùng Pageable để limit)
	@Query("SELECT pn FROM PaymentNotification pn WHERE pn.payment.roomBooking.user.id = :userId ORDER BY pn.sentDate DESC")
	List<PaymentNotification> findRecentByUserId(@Param("userId") Integer userId,
			org.springframework.data.domain.Pageable pageable);
}
