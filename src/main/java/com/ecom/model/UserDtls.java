package com.ecom.model;

import java.time.LocalDateTime;
import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
public class UserDtls {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String name;

	private String mobileNumber;

	@Column(unique = true, nullable = false)
	private String email;

	private String address;

	private String city;

	private String state;

	private String pincode;

	private String password;

	private String profileImage;

	private String role;

	private Boolean isEnable;

	private Boolean accountNonLocked;

	private Integer failedAttempt;

	private Date lockTime;
	
	private String resetToken;

	private String school;

	private String accountType; // 'renter' hoặc 'owner'

	private String bankId; // Mã ngân hàng (MB, VCB, TCB, etc.)

	private String accountNo; // Số tài khoản ngân hàng

	private LocalDateTime createdDate; // Ngày đăng ký tài khoản

	@PrePersist
	public void prePersist() {
		if (this.createdDate == null) {
			this.createdDate = LocalDateTime.now();
		}
	}
}
