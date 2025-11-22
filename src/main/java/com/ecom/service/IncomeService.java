package com.ecom.service;

import java.time.LocalDate;
import java.util.List;

import com.ecom.model.IncomeRecord;

/**
 * Service interface for Income tracking
 * Follows Interface Segregation Principle
 */
public interface IncomeService {

    /**
     * Save a new income record
     */
    IncomeRecord saveIncome(IncomeRecord income);

    /**
     * Get income by ID
     */
    IncomeRecord getIncomeById(Integer id);

    /**
     * Get all income records for an owner
     */
    List<IncomeRecord> getIncomeByOwnerId(Integer ownerId);

    /**
     * Get income by source for an owner
     */
    List<IncomeRecord> getIncomeByOwnerIdAndSource(Integer ownerId, String source);

    /**
     * Get income for a specific room
     */
    List<IncomeRecord> getIncomeByRoomId(Integer roomId);

    /**
     * Get income within a date range
     */
    List<IncomeRecord> getIncomeByDateRange(Integer ownerId, LocalDate startDate, LocalDate endDate);

    /**
     * Get total income for an owner
     */
    Double getTotalIncomeByOwnerId(Integer ownerId);

    /**
     * Get total income by source
     */
    Double getTotalIncomeBySource(Integer ownerId, String source);

    /**
     * Auto-create income record from payment
     * Called when a payment is marked as PAID
     */
    IncomeRecord createIncomeFromPayment(Integer paymentId);

    /**
     * Delete an income record
     */
    Boolean deleteIncome(Integer id);
}
