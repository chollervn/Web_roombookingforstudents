package com.ecom.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.Expense;

/**
 * Repository for Expense entity
 */
public interface ExpenseRepository extends JpaRepository<Expense, Integer> {

    // Find all expenses for a specific owner
    List<Expense> findByOwnerIdOrderByExpenseDateDesc(Integer ownerId);

    // Find expenses by type and owner
    List<Expense> findByOwnerIdAndType(Integer ownerId, String type);

    // Find expenses for a specific room
    List<Expense> findByRoomIdOrderByExpenseDateDesc(Integer roomId);

    // Find expenses within a date range
    @Query("SELECT e FROM Expense e WHERE e.ownerId = :ownerId AND e.expenseDate BETWEEN :startDate AND :endDate ORDER BY e.expenseDate DESC")
    List<Expense> findByOwnerIdAndDateRange(@Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get total expenses for an owner
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.ownerId = :ownerId")
    Double getTotalExpensesByOwnerId(@Param("ownerId") Integer ownerId);

    // Get total expenses by owner and type
    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e WHERE e.ownerId = :ownerId AND e.type = :type")
    Double getTotalExpensesByOwnerIdAndType(@Param("ownerId") Integer ownerId, @Param("type") String type);

    // Get expenses by owner and date range, grouped by type
    @Query("SELECT e.type, SUM(e.amount) FROM Expense e WHERE e.ownerId = :ownerId AND e.expenseDate BETWEEN :startDate AND :endDate GROUP BY e.type")
    List<Object[]> getExpensesByTypeForPeriod(@Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
