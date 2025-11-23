package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Deposit;
import com.ecom.model.Room;
import com.ecom.model.RoomBooking;
import com.ecom.model.RoomStatus;
import com.ecom.repository.DepositRepository;
import com.ecom.repository.RoomBookingRepository;
import com.ecom.repository.RoomRepository;
import com.ecom.service.DepositService;

@Service
public class DepositServiceImpl implements DepositService {

	@Autowired
	private DepositRepository depositRepository;

	@Autowired
	private RoomRepository roomRepository;

	@Autowired
	private com.ecom.repository.RoomOrderRepository roomOrderRepository;

	@Autowired
	private RoomBookingRepository roomBookingRepository;

	@Autowired
	private com.ecom.repository.CartRepository cartRepository;

	@Override
	public Deposit saveDeposit(Deposit deposit) {
		Deposit savedDeposit = depositRepository.save(deposit);
		Room room = savedDeposit.getRoom();
		if (room != null) {
			room.setRoomStatus(RoomStatus.RESERVED); // Khi user đặt cọc, phòng chuyển sang RESERVED
			roomRepository.save(room);
		}
		// Tối ưu: Tạo RoomOrder nếu chưa có cho user/phòng này
		com.ecom.model.RoomOrder order = roomOrderRepository.findByUserIdAndRoomId(deposit.getUser().getId(),
				room.getId());
		if (order == null) {
			order = new com.ecom.model.RoomOrder();
			order.setUser(deposit.getUser());
			order.setRoom(room);
			order.setOrderDate(deposit.getDepositDate());
			order.setPrice(room.getMonthlyRent());
			order.setQuantity(1); // Mặc định 1 tháng
			order.setStatus("PENDING");
			order.setPaymentType(deposit.getPaymentMethod());
			roomOrderRepository.save(order);
		}
		return savedDeposit;
	}

	@Override
	public List<Deposit> getDepositsByUser(Integer userId) {
		return depositRepository.findByUserId(userId);
	}

	@Override
	public List<Deposit> getDepositsByRoom(Integer roomId) {
		return depositRepository.findByRoomId(roomId);
	}

	@Override
	public List<Deposit> getDepositsByOwner(Integer ownerId) {
		return depositRepository.findByRoomOwnerId(ownerId);
	}

	@Override
	public Deposit getDepositById(Integer id) {
		Optional<Deposit> deposit = depositRepository.findById(id);
		return deposit.orElse(null);
	}

	@Override
	public Deposit updateDepositStatus(Integer id, String status, String adminNote) {
		Optional<Deposit> findById = depositRepository.findById(id);
		if (findById.isPresent()) {
			Deposit deposit = findById.get();
			deposit.setStatus(status);
			deposit.setAdminNote(adminNote);
			Room room = deposit.getRoom();

			if ("APPROVED".equals(status)) {
				deposit.setApprovedDate(LocalDate.now());

				// Cập nhật trạng thái phòng
				if (room != null) {
					room.setRoomStatus(RoomStatus.OCCUPIED);
					room.setIsAvailable(false);
					roomRepository.save(room);
				}

				// Tạo hoặc cập nhật RoomOrder
				com.ecom.model.RoomOrder order = roomOrderRepository.findByUserIdAndRoomId(
						deposit.getUser().getId(), room.getId());

				if (order == null) {
					order = new com.ecom.model.RoomOrder();
					order.setOrderId(UUID.randomUUID().toString());
					order.setUser(deposit.getUser());
					order.setRoom(room);
					order.setOrderDate(LocalDate.now());
					order.setPrice(room.getMonthlyRent());
					order.setQuantity(1); // Mặc định 1 tháng
					order.setPaymentType(deposit.getPaymentMethod());
				}
				order.setStatus("RENTED");
				roomOrderRepository.save(order);

				// Tạo hoặc cập nhật RoomBooking
				RoomBooking booking = roomBookingRepository.findByUserIdAndRoomId(
						deposit.getUser().getId(), room.getId());

				if (booking == null) {
					booking = new RoomBooking();
					booking.setUser(deposit.getUser());
					booking.setRoom(room);
					booking.setStartDate(LocalDate.now());
					booking.setDurationMonths(1); // Mặc định 1 tháng
					booking.setEndDate(LocalDate.now().plusMonths(1));
					booking.setMonthlyRent(room.getMonthlyRent());
					booking.setPaymentMethod(deposit.getPaymentMethod());
					booking.setDepositAmount(deposit.getAmount());
				}
				booking.setStatus("ACTIVE");
				roomBookingRepository.save(booking);

			} else if ("REJECTED".equals(status)) {
				// Reset trạng thái phòng về AVAILABLE
				if (room != null) {
					room.setRoomStatus(RoomStatus.AVAILABLE);
					room.setIsAvailable(true);
					roomRepository.save(room);
				}

				// Hủy RoomOrder nếu có
				com.ecom.model.RoomOrder order = roomOrderRepository.findByUserIdAndRoomId(
						deposit.getUser().getId(), room.getId());
				if (order != null) {
					order.setStatus("CANCELLED");
					roomOrderRepository.save(order);
				}

				// Hủy RoomBooking nếu có
				RoomBooking booking = roomBookingRepository.findByUserIdAndRoomId(
						deposit.getUser().getId(), room.getId());
				if (booking != null) {
					booking.setStatus("CANCELLED");
					roomBookingRepository.save(booking);
				}
			}

			// Tighter Interaction: Clear cart item for this room and user to reflect the
			// finalized state
			if (("APPROVED".equals(status) || "REJECTED".equals(status)) && room != null && deposit.getUser() != null) {
				com.ecom.model.Cart cart = cartRepository.findByRoomIdAndUserId(room.getId(),
						deposit.getUser().getId());
				if (cart != null) {
					cartRepository.delete(cart);
				}
			}

			return depositRepository.save(deposit);
		}
		return null;

	}

	@Override
	public List<Deposit> getAllDeposits() {
		return depositRepository.findAll();
	}

	@Override
	public Boolean deleteDeposit(Integer id) {
		Optional<Deposit> deposit = depositRepository.findById(id);
		if (deposit.isPresent()) {
			depositRepository.delete(deposit.get());
			return true;
		}
		return false;
	}
}
