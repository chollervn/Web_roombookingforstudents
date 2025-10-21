package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.RoomType;

public interface RoomTypeRepository extends JpaRepository<RoomType, Integer> {

	public Boolean existsByName(String name);

	public List<RoomType> findByIsActiveTrue();

}
