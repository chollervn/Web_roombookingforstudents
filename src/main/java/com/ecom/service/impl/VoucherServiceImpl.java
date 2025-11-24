package com.ecom.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.model.UserDtls;
import com.ecom.model.Voucher;
import com.ecom.repository.UserRepository;
import com.ecom.repository.VoucherRepository;
import com.ecom.service.VoucherService;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Autowired
    private UserRepository userRepository;

    private Random random = new Random();

    @Override
    public Voucher createVoucher(Integer userId, Integer discountPercent) {
        UserDtls user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return null;
        }

        Voucher voucher = new Voucher();
        voucher.setCode(generateVoucherCode());
        voucher.setDiscountPercent(discountPercent);
        voucher.setUser(user);
        voucher.setIsUsed(false);
        voucher.setCreatedDate(LocalDateTime.now());
        voucher.setExpiryDate(LocalDateTime.now().plusDays(30)); // 30 days validity

        return voucherRepository.save(voucher);
    }

    @Override
    public List<Voucher> getUserValidVouchers(Integer userId) {
        return voucherRepository.findByUserIdAndIsUsedFalseAndExpiryDateAfterOrderByCreatedDateDesc(
                userId, LocalDateTime.now());
    }

    @Override
    public List<Voucher> getAllUserVouchers(Integer userId) {
        return voucherRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Override
    public Voucher getVoucherByCode(String code) {
        return voucherRepository.findByCode(code).orElse(null);
    }

    @Override
    public Boolean applyVoucher(String code, Integer userId) {
        Voucher voucher = voucherRepository.findByCode(code).orElse(null);

        if (voucher == null) {
            return false;
        }

        // Check if voucher belongs to user
        if (!voucher.getUser().getId().equals(userId)) {
            return false;
        }

        // Check if voucher is valid
        if (!voucher.isValid()) {
            return false;
        }

        // Mark as used
        voucher.setIsUsed(true);
        voucher.setUsedDate(LocalDateTime.now());
        voucherRepository.save(voucher);

        return true;
    }

    @Override
    public String generateVoucherCode() {
        String code;
        do {
            // Generate DUCK + 6 random digits
            code = "DUCK" + String.format("%06d", random.nextInt(1000000));
        } while (voucherRepository.findByCode(code).isPresent());

        return code;
    }
}
