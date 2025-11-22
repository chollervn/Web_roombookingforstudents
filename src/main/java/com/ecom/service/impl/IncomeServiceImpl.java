package com.ecom.service.impl;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.exception.BusinessException;
import com.ecom.model.IncomeRecord;
import com.ecom.model.MonthlyPayment;
import com.ecom.repository.IncomeRecordRepository;
import com.ecom.repository.MonthlyPaymentRepository;
import com.ecom.service.IncomeService;

/**
 * Implementation of IncomeService
 * Follows Single Responsibility Principle - only handles income operations
 */
@Service
public class IncomeServiceImpl implements IncomeService {

    @Autowired
    private IncomeRecordRepository incomeRepository;

    @Autowired
    private MonthlyPaymentRepository paymentRepository;

    @Override
    public IncomeRecord saveIncome(IncomeRecord income) {
        validateIncome(income);
        return incomeRepository.save(income);
    }

    @Override
    public IncomeRecord getIncomeById(Integer id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Income", id));
    }

    @Override
    public List<IncomeRecord> getIncomeByOwnerId(Integer ownerId) {
        return incomeRepository.findByOwnerIdOrderByIncomeDateDesc(ownerId);
    }

    @Override
    public List<IncomeRecord> getIncomeByOwnerIdAndSource(Integer ownerId, String source) {
        return incomeRepository.findByOwnerIdAndSource(ownerId, source);
    }

    @Override
    public List<IncomeRecord> getIncomeByRoomId(Integer roomId) {
        return incomeRepository.findByRoomIdOrderByIncomeDateDesc(roomId);
    }

    @Override
    public List<IncomeRecord> getIncomeByDateRange(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        return incomeRepository.findByOwnerIdAndDateRange(ownerId, startDate, endDate);
    }

    @Override
    public Double getTotalIncomeByOwnerId(Integer ownerId) {
        return incomeRepository.getTotalIncomeByOwnerId(ownerId);
    }

    @Override
    public Double getTotalIncomeBySource(Integer ownerId, String source) {
        return incomeRepository.getTotalIncomeByOwnerIdAndSource(ownerId, source);
    }

    @Override
    public IncomeRecord createIncomeFromPayment(Integer paymentId) {
        MonthlyPayment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> BusinessException.notFound("Payment", paymentId));

        // Check if income already exists for this payment
        IncomeRecord existing = incomeRepository.findByPaymentId(paymentId);
        if (existing != null) {
            // Update existing record
            existing.setAmount(payment.getPaidAmount());
            existing.setIncomeDate(payment.getPaidDate());
            return incomeRepository.save(existing);
        }

        // Create new income record
        IncomeRecord income = new IncomeRecord();
        income.setSource("RENT");
        income.setAmount(payment.getPaidAmount());
        income.setIncomeDate(payment.getPaidDate() != null ? payment.getPaidDate() : LocalDate.now());
        income.setRoom(payment.getRoomBooking() != null ? payment.getRoomBooking().getRoom() : null);
        income.setPayment(payment);
        income.setOwnerId(payment.getRoomBooking() != null && payment.getRoomBooking().getRoom() != null
                ? payment.getRoomBooking().getRoom().getOwnerId()
                : null);
        income.setDescription(String.format("Rent payment for %d/%d", payment.getMonth(), payment.getYear()));

        return incomeRepository.save(income);
    }

    @Override
    public Boolean deleteIncome(Integer id) {
        try {
            incomeRepository.deleteById(id);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Validate income data
     */
    private void validateIncome(IncomeRecord income) {
        if (income.getAmount() == null || income.getAmount() <= 0) {
            throw BusinessException.validationFailed("Income amount must be greater than 0");
        }

        if (income.getSource() == null || income.getSource().isEmpty()) {
            throw BusinessException.validationFailed("Income source is required");
        }

        if (income.getOwnerId() == null) {
            throw BusinessException.validationFailed("Owner ID is required");
        }
    }
}
