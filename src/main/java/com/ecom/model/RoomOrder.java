package com.ecom.model;

import java.time.LocalDate;
import java.util.Date;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class RoomOrder {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private String orderId;

	private LocalDate orderDate;

	@ManyToOne
	private Room room;

	private Double price;

	private Integer quantity; // Số tháng thuê

	@ManyToOne
	private UserDtls user;

	private String status;

	private String paymentType;

	@OneToOne(cascade = CascadeType.ALL)
	private OrderAddress orderAddress;

}
