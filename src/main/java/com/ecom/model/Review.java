package com.ecom.model;

import java.time.LocalDateTime;

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
public class Review {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	private Integer roomId;

	private Integer userId;

	private String userName;

	@Column(length = 2000)
	private String comment;

	private Integer rating; // 1-5 sao

	private LocalDateTime createdDate;

	@PrePersist
	public void prePersist() {
		this.createdDate = LocalDateTime.now();
	}
}

