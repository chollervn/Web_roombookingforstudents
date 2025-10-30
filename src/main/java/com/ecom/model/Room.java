package com.ecom.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Room {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(length = 500)
	private String roomName;

	@Column(length = 5000)
	private String description;

	private String roomType; // Studio, 1 phòng ngủ, 2 phòng ngủ, etc.

	private Double monthlyRent;

	private String address;

	@Column(length = 1000)
	private String fullAddress;

	private String district; // Quận/Huyện

	private String city; // Thành phố (Hà Nội, Hải Phòng, etc.)

	private Double area; // Diện tích (m2)

	private Integer maxOccupants; // Số người ở tối đa

	private String image;

	private Boolean hasWifi;

	private Boolean hasAirConditioner;

	private Boolean hasParking;

	private Boolean hasElevator;

	private Boolean allowPets;

	private Double deposit; // Tiền cọc

	private Double electricityCost; // Giá điện (VND/kWh)
	private Double waterCost; // Giá nước (VND/m3)

	private String contactPhone;

	private String contactName;

	private Boolean isAvailable;

	private Boolean isActive;
	
	private Integer ownerId; // ID của chủ trọ

}
