package com.ecom.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // Find valid (unused and not expired) vouchers for a user
    @Query("SELECT v FROM Voucher v WHERE v.user.id = :userId AND v.isUsed = false AND v.expiryDate > :now ORDER BY v.createdDate DESC")
    List<Voucher> findByUserIdAndIsUsedFalseAndExpiryDateAfterOrderByCreatedDateDesc(
            @Param("userId") Integer userId, @Param("now") LocalDateTime now);

    // Find voucher by code
    Optional<Voucher> findByCode(String code);

    // Find all vouchers for a user (sorted by created date)
    @Query("SELECT v FROM Voucher v WHERE v.user.id = :userId ORDER BY v.createdDate DESC")
    List<Voucher> findByUserIdOrderByCreatedDateDesc(@Param("userId") Integer userId);
}
