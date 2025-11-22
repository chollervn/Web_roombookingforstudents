package com.ecom.service;

import java.time.LocalDate;
import java.util.List;

import com.ecom.dto.ExpenseDTO;
import com.ecom.model.Expense;

/**
 * Service interface for Expense management
 * Follows Interface Segregation Principle
 */
public interface ExpenseService {

    /**
     * Save a new expense
     */
    Expense saveExpense(Expense expense);

    /**
     * Get expense by ID
     */
    Expense getExpenseById(Integer id);

    /**
     * Get all expenses for an owner
     */
    List<Expense> getExpensesByOwnerId(Integer ownerId);

    /**
     * Get expenses by type for an owner
     */
    List<Expense> getExpensesByOwnerIdAndType(Integer ownerId, String type);

    /**
     * Get expenses for a specific room
     */
    List<Expense> getExpensesByRoomId(Integer roomId);

    /**
     * Get expenses within a date range
     */
    List<Expense> getExpensesByDateRange(Integer ownerId, LocalDate startDate, LocalDate endDate);

    /**
     * Get total expenses for an owner
     */
    Double getTotalExpensesByOwnerId(Integer ownerId);

    /**
     * Get total expenses by type
     */
    Double getTotalExpensesByType(Integer ownerId, String type);

    /**
     * Delete an expense
     */
    Boolean deleteExpense(Integer id);

    /**
     * Update an expense
     */
    Expense updateExpense(Expense expense);

    /**
     * Convert Expense entity to DTO
     */
    ExpenseDTO toDTO(Expense expense);

    /**
     * Convert DTO to Expense entity
     */
    Expense toEntity(ExpenseDTO dto);
}
