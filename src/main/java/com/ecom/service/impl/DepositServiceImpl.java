package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Deposit;
import com.ecom.model.Room;
import com.ecom.repository.DepositRepository;
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

	@Override
	public Deposit saveDeposit(Deposit deposit) {
		Deposit savedDeposit = depositRepository.save(deposit);
		Room room = savedDeposit.getRoom();
		if (room != null) {
			room.setStatus("PENDING"); // Khi user đặt cọc, phòng chuyển sang PENDING
			roomRepository.save(room);
		}
		// Tối ưu: Tạo RoomOrder nếu chưa có cho user/phòng này
		com.ecom.model.RoomOrder order = roomOrderRepository.findByUserIdAndRoomId(deposit.getUser().getId(), room.getId());
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
			com.ecom.model.RoomOrder order = roomOrderRepository.findByUserIdAndRoomId(deposit.getUser().getId(), room.getId());
			if ("APPROVED".equals(status)) {
				deposit.setApprovedDate(LocalDate.now());
				if (room != null) {
					room.setStatus("RENTED");
					room.setIsAvailable(false);
					roomRepository.save(room);
				}
				if (order != null) {
					order.setStatus("RENTED");
					roomOrderRepository.save(order);
				}
			} else if ("REJECTED".equals(status)) {
				if (room != null) {
					room.setStatus("ACTIVE");
					room.setIsAvailable(true);
					roomRepository.save(room);
				}
				if (order != null) {
					order.setStatus("CANCELLED");
					roomOrderRepository.save(order);
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
