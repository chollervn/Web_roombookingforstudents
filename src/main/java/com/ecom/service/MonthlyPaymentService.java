package com.ecom.service;

import java.time.LocalDate;
import java.util.List;

import com.ecom.model.MonthlyPayment;

public interface MonthlyPaymentService {
	
	public MonthlyPayment savePayment(MonthlyPayment payment);
	
	public MonthlyPayment getPaymentById(Integer id);
	
	public List<MonthlyPayment> getAllPaymentsByOwnerId(Integer ownerId);
	
	public List<MonthlyPayment> getPaymentsByBookingId(Integer bookingId);
	
	public List<MonthlyPayment> getPaymentsByRoomId(Integer roomId);
	
	public List<MonthlyPayment> getPaymentsByStatus(String status);
	
	public List<MonthlyPayment> getPaymentsByOwnerAndStatus(Integer ownerId, String status);
	
	public List<MonthlyPayment> getOverduePayments();
	
	public List<MonthlyPayment> getUpcomingPayments(Integer days);
	
	public MonthlyPayment recordPayment(Integer paymentId, Double amount, LocalDate paidDate);
	
	public MonthlyPayment updatePaymentStatus(Integer paymentId, String status);
	
	public Boolean deletePayment(Integer id);
	
	public MonthlyPayment createMonthlyPayment(Integer bookingId, Integer month, Integer year);
	
	public void autoGenerateMonthlyPayments(); // Tự động tạo hóa đơn hàng tháng
	
	public Double getTotalRevenueByOwnerId(Integer ownerId);
	
	public Double getRevenueByOwnerAndMonth(Integer ownerId, Integer month, Integer year);
	
	public MonthlyPayment findByBookingAndMonthYear(Integer bookingId, Integer month, Integer year);
}
