package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.MonthlyPayment;
import com.ecom.model.RoomBooking;
import com.ecom.repository.MonthlyPaymentRepository;
import com.ecom.repository.RoomBookingRepository;
import com.ecom.service.MonthlyPaymentService;

@Service
public class MonthlyPaymentServiceImpl implements MonthlyPaymentService {
	
	@Autowired
	private MonthlyPaymentRepository paymentRepository;
	
	@Autowired
	private RoomBookingRepository bookingRepository;
	
	@Override
	public MonthlyPayment savePayment(MonthlyPayment payment) {
		return paymentRepository.save(payment);
	}
	
	@Override
	public MonthlyPayment getPaymentById(Integer id) {
		return paymentRepository.findById(id).orElse(null);
	}
	
	@Override
	public List<MonthlyPayment> getAllPaymentsByOwnerId(Integer ownerId) {
		return paymentRepository.findByOwnerId(ownerId);
	}
	
	@Override
	public List<MonthlyPayment> getPaymentsByBookingId(Integer bookingId) {
		return paymentRepository.findByRoomBooking_IdOrderByYearDescMonthDesc(bookingId);
	}
	
	@Override
	public List<MonthlyPayment> getPaymentsByRoomId(Integer roomId) {
		return paymentRepository.findByRoomId(roomId);
	}
	
	@Override
	public List<MonthlyPayment> getPaymentsByStatus(String status) {
		return paymentRepository.findByStatus(status);
	}
	
	@Override
	public List<MonthlyPayment> getPaymentsByOwnerAndStatus(Integer ownerId, String status) {
		return paymentRepository.findByOwnerIdAndStatus(ownerId, status);
	}
	
	@Override
	public List<MonthlyPayment> getOverduePayments() {
		return paymentRepository.findOverduePayments(LocalDate.now());
	}
	
	@Override
	public List<MonthlyPayment> getUpcomingPayments(Integer days) {
		LocalDate startDate = LocalDate.now();
		LocalDate endDate = startDate.plusDays(days);
		return paymentRepository.findUpcomingPayments(startDate, endDate);
	}
	
	@Override
	public MonthlyPayment recordPayment(Integer paymentId, Double amount, LocalDate paidDate) {
		MonthlyPayment payment = paymentRepository.findById(paymentId).orElse(null);
		if (payment == null) {
			return null;
		}
		
		payment.setPaidAmount(payment.getPaidAmount() + amount);
		payment.setPaidDate(paidDate);
		
		// Cập nhật trạng thái
		if (payment.getPaidAmount() >= payment.getAmount()) {
			payment.setStatus("PAID");
		} else if (payment.getPaidAmount() > 0) {
			payment.setStatus("PARTIAL");
		}
		
		return paymentRepository.save(payment);
	}
	
	@Override
	public MonthlyPayment updatePaymentStatus(Integer paymentId, String status) {
		MonthlyPayment payment = paymentRepository.findById(paymentId).orElse(null);
		if (payment != null) {
			payment.setStatus(status);
			return paymentRepository.save(payment);
		}
		return null;
	}
	
	@Override
	public Boolean deletePayment(Integer id) {
		MonthlyPayment payment = paymentRepository.findById(id).orElse(null);
		if (!ObjectUtils.isEmpty(payment)) {
			paymentRepository.delete(payment);
			return true;
		}
		return false;
	}
	
	@Override
	public MonthlyPayment createMonthlyPayment(Integer bookingId, Integer month, Integer year) {
		RoomBooking booking = bookingRepository.findById(bookingId).orElse(null);
		if (booking == null) {
			return null;
		}
		
		// Kiểm tra xem payment đã tồn tại chưa
		MonthlyPayment existingPayment = paymentRepository.findByBookingAndMonthYear(bookingId, month, year);
		if (existingPayment != null) {
			return existingPayment; // Đã tồn tại
		}
		
		MonthlyPayment payment = new MonthlyPayment();
		payment.setRoomBooking(booking);
		payment.setMonth(month);
		payment.setYear(year);
		payment.setAmount(booking.getMonthlyRent());
		payment.setPaidAmount(0.0);
		payment.setStatus("PENDING");
		
		// Đặt ngày đến hạn là ngày 5 của tháng
		payment.setDueDate(LocalDate.of(year, month, 5));
		
		return paymentRepository.save(payment);
	}
	
	@Override
	public void autoGenerateMonthlyPayments() {
		// Lấy tất cả bookings đang active
		List<RoomBooking> activeBookings = bookingRepository.findByStatus("ACTIVE");
		
		LocalDate now = LocalDate.now();
		int currentMonth = now.getMonthValue();
		int currentYear = now.getYear();
		
		for (RoomBooking booking : activeBookings) {
			// Tạo payment cho tháng hiện tại nếu chưa có
			MonthlyPayment existingPayment = paymentRepository.findByBookingAndMonthYear(
				booking.getId(), currentMonth, currentYear
			);
			
			if (existingPayment == null) {
				createMonthlyPayment(booking.getId(), currentMonth, currentYear);
			}
		}
	}
	
	@Override
	public Double getTotalRevenueByOwnerId(Integer ownerId) {
		return paymentRepository.getTotalRevenueByOwnerId(ownerId);
	}
	
	@Override
	public Double getRevenueByOwnerAndMonth(Integer ownerId, Integer month, Integer year) {
		return paymentRepository.getRevenueByOwnerAndMonthYear(ownerId, month, year);
	}
	
	@Override
	public MonthlyPayment findByBookingAndMonthYear(Integer bookingId, Integer month, Integer year) {
		return paymentRepository.findByBookingAndMonthYear(bookingId, month, year);
	}
}
