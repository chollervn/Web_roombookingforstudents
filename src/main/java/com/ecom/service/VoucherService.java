package com.ecom.service;

import java.util.List;

import com.ecom.model.Voucher;

public interface VoucherService {

    // Create a new voucher for a user
    Voucher createVoucher(Long userId, Integer discountPercent);

    // Get valid vouchers for a user (not used and not expired)
    List<Voucher> getUserValidVouchers(Long userId);

    // Get all vouchers for a user
    List<Voucher> getAllUserVouchers(Long userId);

    // Get voucher by code
    Voucher getVoucherByCode(String code);

    // Apply voucher (mark as used)
    Boolean applyVoucher(String code, Long userId);

    // Generate unique voucher code
    String generateVoucherCode();
}
