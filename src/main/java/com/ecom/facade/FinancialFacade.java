package com.ecom.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.builder.FinancialReportBuilder;
import com.ecom.dto.FinancialReportDTO;
import com.ecom.model.Expense;
import com.ecom.model.IncomeRecord;
import com.ecom.model.MonthlyPayment;
import com.ecom.model.Room;
import com.ecom.service.ExpenseService;
import com.ecom.service.IncomeService;
import com.ecom.service.MonthlyPaymentService;
import com.ecom.service.RoomService;

/**
 * Facade Pattern for Financial Operations
 * Simplifies complex financial operations by providing a unified interface
 * Coordinates between Income, Expense, and Payment services
 * Follows Facade Pattern principles
 */
@Service
public class FinancialFacade {

    @Autowired
    private IncomeService incomeService;

    @Autowired
    private ExpenseService expenseService;

    @Autowired
    private MonthlyPaymentService paymentService;

    @Autowired
    private RoomService roomService;

    /**
     * Generate comprehensive financial report for an owner
     * This method demonstrates Facade pattern - hides complexity of gathering data
     * from multiple services
     */
    public FinancialReportDTO generateFinancialReport(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        // Gather data from multiple services
        List<IncomeRecord> income = incomeService.getIncomeByDateRange(ownerId, startDate, endDate);
        List<Expense> expenses = expenseService.getExpensesByDateRange(ownerId, startDate, endDate);
        List<MonthlyPayment> payments = paymentService.getAllPaymentsByOwnerId(ownerId);

        // Filter payments by date range
        List<MonthlyPayment> periodPayments = payments.stream()
                .filter(p -> {
                    if (p.getDueDate() != null) {
                        return !p.getDueDate().isBefore(startDate) && !p.getDueDate().isAfter(endDate);
                    }
                    return false;
                })
                .toList();

        // Calculate payment statistics
        int totalPayments = periodPayments.size();
        int paidPayments = (int) periodPayments.stream().filter(p -> "PAID".equals(p.getStatus())).count();
        int pendingPayments = (int) periodPayments.stream().filter(p -> "PENDING".equals(p.getStatus())).count();
        int overduePayments = (int) periodPayments.stream().filter(p -> "OVERDUE".equals(p.getStatus())).count();

        // Get property statistics
        List<Room> ownerRooms = roomService.getRoomsByOwnerId(ownerId);
        int totalRooms = ownerRooms.size();
        int occupiedRooms = (int) ownerRooms.stream()
                .filter(r -> r.getIsAvailable() != null && !r.getIsAvailable())
                .count();

        // Use Builder pattern to construct the report
        return new FinancialReportBuilder()
                .forOwner(ownerId)
                .forPeriod(startDate, endDate)
                .withIncome(income)
                .withExpenses(expenses)
                .includeIncome()
                .includeExpenses()
                .withProfitCalculation()
                .withPaymentStats(totalPayments, paidPayments, pendingPayments, overduePayments)
                .withPropertyStats(totalRooms, occupiedRooms)
                .build();
    }

    /**
     * Get current month financial summary
     */
    public FinancialReportDTO getCurrentMonthSummary(Integer ownerId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfMonth = now.withDayOfMonth(1);
        LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

        return generateFinancialReport(ownerId, startOfMonth, endOfMonth);
    }

    /**
     * Get year-to-date financial summary
     */
    public FinancialReportDTO getYearToDateSummary(Integer ownerId) {
        LocalDate now = LocalDate.now();
        LocalDate startOfYear = now.withDayOfYear(1);

        return generateFinancialReport(ownerId, startOfYear, now);
    }

    /**
     * Record payment and create income
     * Demonstrates coordination between multiple services
     */
    public void recordPaymentAndIncome(Integer paymentId, Double amount, LocalDate paidDate) {
        // Record the payment
        MonthlyPayment payment = paymentService.recordPayment(paymentId, amount, paidDate);

        // Auto-create income record if payment is fully paid
        if ("PAID".equals(payment.getStatus())) {
            incomeService.createIncomeFromPayment(paymentId);
        }
    }

    /**
     * Get expense breakdown by type
     */
    public Map<String, Double> getExpenseBreakdown(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        List<Expense> expenses = expenseService.getExpensesByDateRange(ownerId, startDate, endDate);

        return new FinancialReportBuilder()
                .withExpenses(expenses)
                .includeExpenses()
                .build()
                .getExpenseByCategory();
    }

    /**
     * Get income breakdown by source
     */
    public Map<String, Double> getIncomeBreakdown(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        List<IncomeRecord> income = incomeService.getIncomeByDateRange(ownerId, startDate, endDate);

        return new FinancialReportBuilder()
                .withIncome(income)
                .includeIncome()
                .build()
                .getIncomeByCategory();
    }

    /**
     * Get net profit for a period
     */
    public Double getNetProfit(Integer ownerId, LocalDate startDate, LocalDate endDate) {
        Double totalIncome = incomeService.getIncomeByDateRange(ownerId, startDate, endDate)
                .stream()
                .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0.0)
                .sum();

        Double totalExpenses = expenseService.getExpensesByDateRange(ownerId, startDate, endDate)
                .stream()
                .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                .sum();

        return totalIncome - totalExpenses;
    }
}
