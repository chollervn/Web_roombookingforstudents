package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.Deposit;
import com.ecom.repository.DepositRepository;
import com.ecom.service.DepositService;

@Service
public class DepositServiceImpl implements DepositService {

	@Autowired
	private DepositRepository depositRepository;

	@Override
	public Deposit saveDeposit(Deposit deposit) {
		return depositRepository.save(deposit);
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
			if ("APPROVED".equals(status)) {
				deposit.setApprovedDate(LocalDate.now());
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
