package com.ecom.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
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
public class PaymentNotification {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne
	private MonthlyPayment payment; // Liên kết với payment
	
	private LocalDateTime sentDate; // Ngày gửi thông báo
	
	private String type; // EMAIL, SMS, IN_APP
	
	private String status; // SENT, FAILED, READ
	
	@Column(length = 2000)
	private String message; // Nội dung thông báo
	
	private String recipientEmail; // Email người nhận
	
	private String recipientPhone; // SĐT người nhận
	
	@PrePersist
	public void prePersist() {
		this.sentDate = LocalDateTime.now();
		if (this.status == null) {
			this.status = "SENT";
		}
		if (this.type == null) {
			this.type = "IN_APP";
		}
	}
}
