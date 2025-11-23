package com.ecom.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecom.model.Voucher;

public interface VoucherRepository extends JpaRepository<Voucher, Long> {

    // Find valid (unused and not expired) vouchers for a user
    List<Voucher> findByUserIdAndIsUsedFalseAndExpiryDateAfterOrderByCreatedDateDesc(
            Long userId, LocalDateTime now);

    // Find voucher by code
    Optional<Voucher> findByCode(String code);

    // Find all vouchers for a user (sorted by created date)
    List<Voucher> findByUserIdOrderByCreatedDateDesc(Long userId);
}
