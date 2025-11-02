package com.ecom.service;

import java.util.List;

import com.ecom.model.Deposit;

public interface DepositService {

	public Deposit saveDeposit(Deposit deposit);

	public List<Deposit> getDepositsByUser(Integer userId);

	public List<Deposit> getDepositsByRoom(Integer roomId);

	public List<Deposit> getDepositsByOwner(Integer ownerId);

	public Deposit getDepositById(Integer id);

	public Deposit updateDepositStatus(Integer id, String status, String adminNote);

	public List<Deposit> getAllDeposits();

	public Boolean deleteDeposit(Integer id);
}
