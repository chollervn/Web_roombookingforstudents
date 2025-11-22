package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.dto.ExpenseDTO;
import com.ecom.exception.BusinessException;
import com.ecom.model.Expense;
import com.ecom.repository.ExpenseRepository;
import com.ecom.repository.RoomRepository;
import com.ecom.service.ExpenseService;

/**
 * Implementation of ExpenseService
 * Follows Single Responsibility Principle - only handles expense operations
 */
@Service
public class ExpenseServiceImpl implements ExpenseService {

    @Autowired
    private ExpenseRepository expenseRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public Expense saveExpense(Expense expense) {
        validateExpense(expense);
        return expenseRepository.save(expense);
    }

    @Override
    public Expense getExpenseById(Integer id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Expense", id));
    }

    @Override
    public List<Expense> getExpensesByOwnerId(Integer ownerId) {
        return expenseRepository.findByOwnerIdOrderByExpenseDateDesc(ownerId);
    }

    @Override
    public List<Expense> getExpensesByOwnerIdAndType(Integer ownerId, String type) {
        return expenseRepository.findByOwnerIdAndType(ownerId, type);
    }

    @Override
    public List<Expense> getExpensesByRoomId(Integer roomId) {
        return expenseRepository.findByRoomIdOrderByExpenseDateDesc(roomId);
    }

    @Override
    public List<Expense> getExpensesByDateRange(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        return expenseRepository.findByOwnerIdAndDateRange(ownerId, startDate, endDate);
    }

    @Override
    public Double getTotalExpensesByOwnerId(Integer ownerId) {
        return expenseRepository.getTotalExpensesByOwnerId(ownerId);
    }

    @Override
    public Double getTotalExpensesByType(Integer ownerId, String type) {
        return expenseRepository.getTotalExpensesByOwnerIdAndType(ownerId, type);
    }

    @Override
    public Boolean deleteExpense(Integer id) {
        try {
            expenseRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public Expense updateExpense(Expense expense) {
        if (expense.getId() == null) {
            throw BusinessException.validationFailed("Expense ID is required for update");
        }

        // Verify expense exists
        expenseRepository.findById(expense.getId())
                .orElseThrow(() -> BusinessException.notFound("Expense", expense.getId()));

        validateExpense(expense);
        return expenseRepository.save(expense);
    }

    @Override
    public ExpenseDTO toDTO(Expense expense) {
        if (expense == null) {
            return null;
        }

        return ExpenseDTO.builder()
                .id(expense.getId())
                .type(expense.getType())
                .category(expense.getCategory())
                .amount(expense.getAmount())
                .expenseDate(expense.getExpenseDate())
                .roomId(expense.getRoom() != null ? expense.getRoom().getId() : null)
                .roomName(expense.getRoom() != null ? expense.getRoom().getRoomName() : null)
                .description(expense.getDescription())
                .receiptImage(expense.getReceiptImage())
                .paymentMethod(expense.getPaymentMethod())
                .vendor(expense.getVendor())
                .build();
    }

    @Override
    public Expense toEntity(ExpenseDTO dto) {
        if (dto == null) {
            return null;
        }

        Expense expense = new Expense();
        expense.setId(dto.getId());
        expense.setType(dto.getType());
        expense.setCategory(dto.getCategory());
        expense.setAmount(dto.getAmount());
        expense.setExpenseDate(dto.getExpenseDate());
        expense.setDescription(dto.getDescription());
        expense.setReceiptImage(dto.getReceiptImage());
        expense.setPaymentMethod(dto.getPaymentMethod());
        expense.setVendor(dto.getVendor());

        // Set room if roomId is provided
        if (dto.getRoomId() != null) {
            roomRepository.findById(dto.getRoomId()).ifPresent(expense::setRoom);
        }

        return expense;
    }

    /**
     * Validate expense data
     */
    private void validateExpense(Expense expense) {
        if (expense.getAmount() == null || expense.getAmount() <= 0) {
            throw BusinessException.validationFailed("Expense amount must be greater than 0");
        }

        if (expense.getType() == null || expense.getType().isEmpty()) {
            throw BusinessException.validationFailed("Expense type is required");
        }

        if (expense.getOwnerId() == null) {
            throw BusinessException.validationFailed("Owner ID is required");
        }
    }
}
