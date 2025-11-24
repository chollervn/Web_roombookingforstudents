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
public class Deposit {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@ManyToOne
	private UserDtls user; // Người đặt cọc

	@ManyToOne
	private Room room; // Phòng được đặt cọc

	private Double amount; // Số tiền đặt cọc

	private LocalDate depositDate; // Ngày đặt cọc

	private String status; // Trạng thái: PENDING, APPROVED, REJECTED, REFUNDED

	private String paymentMethod; // Phương thức thanh toán: CASH, BANK_TRANSFER, E_WALLET

	private String note; // Ghi chú từ người đặt cọc

	private String adminNote; // Ghi chú từ admin/chủ trọ

	private LocalDate approvedDate; // Ngày chủ trọ xác nhận

	@ManyToOne
	private Voucher voucher; // Voucher được sử dụng (nếu có)

	private Double discountAmount; // Số tiền được giảm giá

	private Double originalAmount; // Số tiền gốc trước khi giảm giá

	private Boolean isNotificationSeen = false; // Đã xem thông báo chưa
}
