package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.ecom.model.RoomType;
import com.ecom.repository.RoomTypeRepository;
import com.ecom.service.RoomTypeService;

@Service
public class RoomTypeServiceImpl implements RoomTypeService {

	@Autowired
	private RoomTypeRepository roomTypeRepository;

	@Override
	public RoomType saveRoomType(RoomType roomType) {
		return roomTypeRepository.save(roomType);
	}

	@Override
	public List<RoomType> getAllRoomType() {
		return roomTypeRepository.findAll();
	}

	@Override
	public Boolean existRoomType(String name) {
		return roomTypeRepository.existsByName(name);
	}

	@Override
	public Boolean deleteRoomType(int id) {
		RoomType roomType = roomTypeRepository.findById(id).orElse(null);

		if (!ObjectUtils.isEmpty(roomType)) {
			roomTypeRepository.delete(roomType);
			return true;
		}
		return false;
	}

	@Override
	public RoomType getRoomTypeById(int id) {
		RoomType roomType = roomTypeRepository.findById(id).orElse(null);
		return roomType;
	}

	@Override
	public List<RoomType> getAllActiveRoomType() {
		List<RoomType> roomTypes = roomTypeRepository.findByIsActiveTrue();
		return roomTypes;
	}

	@Override
	public Page<RoomType> getAllRoomTypePagination(Integer pageNo, Integer pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return roomTypeRepository.findAll(pageable);
	}

}
