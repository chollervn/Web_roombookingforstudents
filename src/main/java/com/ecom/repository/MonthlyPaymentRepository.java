package com.ecom.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.MonthlyPayment;

public interface MonthlyPaymentRepository extends JpaRepository<MonthlyPayment, Integer> {
	
	// Lấy tất cả payments của một owner (thông qua roomBooking -> room -> ownerId)
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.roomBooking.room.ownerId = :ownerId ORDER BY mp.year DESC, mp.month DESC")
	List<MonthlyPayment> findByOwnerId(@Param("ownerId") Integer ownerId);
	
	// Lấy payments theo booking ID
	List<MonthlyPayment> findByRoomBooking_IdOrderByYearDescMonthDesc(Integer bookingId);
	
	// Lấy payments theo phòng
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.roomBooking.room.id = :roomId ORDER BY mp.year DESC, mp.month DESC")
	List<MonthlyPayment> findByRoomId(@Param("roomId") Integer roomId);
	
	// Lấy theo trạng thái
	List<MonthlyPayment> findByStatus(String status);
	
	// Lấy theo trạng thái của owner cụ thể
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.roomBooking.room.ownerId = :ownerId AND mp.status = :status ORDER BY mp.dueDate ASC")
	List<MonthlyPayment> findByOwnerIdAndStatus(@Param("ownerId") Integer ownerId, @Param("status") String status);
	
	// Tìm payment quá hạn
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.dueDate < :date AND mp.status IN ('PENDING', 'PARTIAL')")
	List<MonthlyPayment> findOverduePayments(@Param("date") LocalDate date);
	
	// Tìm payment sắp đến hạn (trong X ngày tới)
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.dueDate BETWEEN :startDate AND :endDate AND mp.status = 'PENDING'")
	List<MonthlyPayment> findUpcomingPayments(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
	
	// Lấy payment theo tháng/năm và booking
	@Query("SELECT mp FROM MonthlyPayment mp WHERE mp.roomBooking.id = :bookingId AND mp.month = :month AND mp.year = :year")
	MonthlyPayment findByBookingAndMonthYear(@Param("bookingId") Integer bookingId, @Param("month") Integer month, @Param("year") Integer year);
	
	// Tổng doanh thu đã thu của owner
	@Query("SELECT COALESCE(SUM(mp.paidAmount), 0) FROM MonthlyPayment mp WHERE mp.roomBooking.room.ownerId = :ownerId AND mp.status IN ('PAID', 'PARTIAL')")
	Double getTotalRevenueByOwnerId(@Param("ownerId") Integer ownerId);
	
	// Tổng doanh thu theo tháng/năm
	@Query("SELECT COALESCE(SUM(mp.paidAmount), 0) FROM MonthlyPayment mp WHERE mp.roomBooking.room.ownerId = :ownerId AND mp.month = :month AND mp.year = :year AND mp.status IN ('PAID', 'PARTIAL')")
	Double getRevenueByOwnerAndMonthYear(@Param("ownerId") Integer ownerId, @Param("month") Integer month, @Param("year") Integer year);
}
