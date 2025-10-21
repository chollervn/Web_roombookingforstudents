package com.ecom.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.ecom.model.RoomType;

public interface RoomTypeService {

	public RoomType saveRoomType(RoomType roomType);

	public Boolean existRoomType(String name);

	public List<RoomType> getAllRoomType();

	public Boolean deleteRoomType(int id);

	public RoomType getRoomTypeById(int id);

	public List<RoomType> getAllActiveRoomType();

	public Page<RoomType> getAllRoomTypePagination(Integer pageNo,Integer pageSize);

}
