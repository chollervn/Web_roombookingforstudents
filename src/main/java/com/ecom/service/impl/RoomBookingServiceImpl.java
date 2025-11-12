package com.ecom.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.RoomBooking;
import com.ecom.repository.RoomBookingRepository;
import com.ecom.service.RoomBookingService;

@Service
public class RoomBookingServiceImpl implements RoomBookingService {

	@Autowired
	private RoomBookingRepository bookingRepository;

	@Override
	public RoomBooking saveBooking(RoomBooking booking) {
		return bookingRepository.save(booking);
	}

	@Override
	public List<RoomBooking> getBookingsByUser(Integer userId) {
		return bookingRepository.findByUserId(userId);
	}

	@Override
	public List<RoomBooking> getBookingsByRoom(Integer roomId) {
		return bookingRepository.findByRoomId(roomId);
	}

	@Override
	public List<RoomBooking> getBookingsByOwner(Integer ownerId) {
		return bookingRepository.findByRoomOwnerId(ownerId);
	}

	@Override
	public RoomBooking getBookingById(Integer id) {
		Optional<RoomBooking> booking = bookingRepository.findById(id);
		return booking.orElse(null);
	}

	@Override
	public RoomBooking updateBookingStatus(Integer id, String status) {
		Optional<RoomBooking> findById = bookingRepository.findById(id);
		if (findById.isPresent()) {
			RoomBooking booking = findById.get();
			booking.setStatus(status);
			return bookingRepository.save(booking);
		}
		return null;
	}

	@Override
	public List<RoomBooking> getAllBookings() {
		return bookingRepository.findAll();
	}

	@Override
	public Boolean deleteBooking(Integer id) {
		Optional<RoomBooking> booking = bookingRepository.findById(id);
		if (booking.isPresent()) {
			bookingRepository.delete(booking.get());
			return true;
		}
		return false;
	}
}

