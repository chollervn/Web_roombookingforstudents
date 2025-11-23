package com.ecom.model;

import java.time.LocalDate;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RoomBooking {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private UserDtls user; // Người thuê trọ

	@ManyToOne
	private Room room; // Phòng được thuê

	private LocalDate startDate; // Ngày bắt đầu thuê

	private LocalDate endDate; // Ngày hết hạn thuê

	private Double monthlyRent; // Giá thuê hàng tháng

	private Integer durationMonths; // Số tháng thuê

	private String status; // ACTIVE, CANCELLED, EXPIRED

	private String paymentMethod; // Phương thức thanh toán

	private Double depositAmount; // Số tiền đã đặt cọc

	private String note; // Ghi chú

	@jakarta.persistence.Transient
	private String rentalStatus; // Trạng thái hiển thị (Active, Expiring, Terminated, Pending Payment)
}
