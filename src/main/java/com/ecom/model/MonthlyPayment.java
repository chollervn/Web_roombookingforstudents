package com.ecom.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class MonthlyPayment {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private RoomBooking roomBooking; // Liên kết với booking
	
	private Integer month; // Tháng thanh toán (1-12)
	
	private Integer year; // Năm thanh toán
	
	private Double amount; // Tổng số tiền phải trả
	
	private Double paidAmount; // Số tiền đã trả
	
	private String status; // PENDING, PAID, OVERDUE, PARTIAL
	
	private LocalDate dueDate; // Ngày đến hạn thanh toán
	
	private LocalDate paidDate; // Ngày thực tế thanh toán
	
	private String note; // Ghi chú
	
	private Double electricityUsed; // Điện tiêu thụ (kWh)
	
	private Double waterUsed; // Nước tiêu thụ (m3)
	
	private Double additionalFees; // Phí phát sinh khác
	
	private LocalDateTime createdDate; // Ngày tạo hóa đơn
	
	@PrePersist
	public void prePersist() {
		this.createdDate = LocalDateTime.now();
		if (this.status == null) {
			this.status = "PENDING";
		}
		if (this.paidAmount == null) {
			this.paidAmount = 0.0;
		}
	}
	
	// Helper method để tính tổng tiền
	public Double calculateTotalAmount(Double electricityCost, Double waterCost) {
		Double total = this.roomBooking != null ? this.roomBooking.getMonthlyRent() : 0.0;
		
		if (electricityUsed != null && electricityCost != null) {
			total += electricityUsed * electricityCost;
		}
		
		if (waterUsed != null && waterCost != null) {
			total += waterUsed * waterCost;
		}
		
		if (additionalFees != null) {
			total += additionalFees;
		}
		
		return total;
	}
}
