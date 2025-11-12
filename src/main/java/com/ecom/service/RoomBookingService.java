package com.ecom.service;

import java.util.List;

import com.ecom.model.RoomBooking;

public interface RoomBookingService {

	public RoomBooking saveBooking(RoomBooking booking);

	public List<RoomBooking> getBookingsByUser(Integer userId);

	public List<RoomBooking> getBookingsByRoom(Integer roomId);

	public List<RoomBooking> getBookingsByOwner(Integer ownerId);

	public RoomBooking getBookingById(Integer id);

	public RoomBooking updateBookingStatus(Integer id, String status);

	public List<RoomBooking> getAllBookings();

	public Boolean deleteBooking(Integer id);
}

