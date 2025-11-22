package com.ecom.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.IncomeRecord;

/**
 * Repository for IncomeRecord entity
 */
public interface IncomeRecordRepository extends JpaRepository<IncomeRecord, Integer> {

    // Find all income records for a specific owner
    List<IncomeRecord> findByOwnerIdOrderByIncomeDateDesc(Integer ownerId);

    // Find income by source and owner
    List<IncomeRecord> findByOwnerIdAndSource(Integer ownerId, String source);

    // Find income for a specific room
    List<IncomeRecord> findByRoomIdOrderByIncomeDateDesc(Integer roomId);

    // Find income within a date range
    @Query("SELECT i FROM IncomeRecord i WHERE i.ownerId = :ownerId AND i.incomeDate BETWEEN :startDate AND :endDate ORDER BY i.incomeDate DESC")
    List<IncomeRecord> findByOwnerIdAndDateRange(@Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Get total income for an owner
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeRecord i WHERE i.ownerId = :ownerId")
    Double getTotalIncomeByOwnerId(@Param("ownerId") Integer ownerId);

    // Get total income by owner and source
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeRecord i WHERE i.ownerId = :ownerId AND i.source = :source")
    Double getTotalIncomeByOwnerIdAndSource(@Param("ownerId") Integer ownerId, @Param("source") String source);

    // Get income by owner and date range, grouped by source
    @Query("SELECT i.source, SUM(i.amount) FROM IncomeRecord i WHERE i.ownerId = :ownerId AND i.incomeDate BETWEEN :startDate AND :endDate GROUP BY i.source")
    List<Object[]> getIncomeBySourceForPeriod(@Param("ownerId") Integer ownerId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    // Find income by payment ID
    IncomeRecord findByPaymentId(Integer paymentId);
}
