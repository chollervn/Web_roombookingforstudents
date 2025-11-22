package com.ecom.builder;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ecom.dto.FinancialReportDTO;
import com.ecom.model.Expense;
import com.ecom.model.IncomeRecord;

/**
 * Builder Pattern for creating Financial Reports
 * Allows flexible, step-by-step construction of complex reports
 * Follows Builder Pattern principles
 */
public class FinancialReportBuilder {

    private FinancialReportDTO report;
    private List<IncomeRecord> incomeRecords;
    private List<Expense> expenses;

    public FinancialReportBuilder() {
        this.report = new FinancialReportDTO();
    }

    /**
     * Set the owner for this report
     */
    public FinancialReportBuilder forOwner(Integer ownerId) {
        report.setOwnerId(ownerId);
        return this;
    }

    /**
     * Set the date range for this report
     */
    public FinancialReportBuilder forPeriod(LocalDate startDate, LocalDate endDate) {
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        return this;
    }

    /**
     * Set income data
     */
    public FinancialReportBuilder withIncome(List<IncomeRecord> incomeRecords) {
        this.incomeRecords = incomeRecords;
        return this;
    }

    /**
     * Set expense data
     */
    public FinancialReportBuilder withExpenses(List<Expense> expenses) {
        this.expenses = expenses;
        return this;
    }

    /**
     * Include income analysis in the report
     */
    public FinancialReportBuilder includeIncome() {
        if (incomeRecords != null) {
            // Calculate total income
            Double total = incomeRecords.stream()
                    .mapToDouble(i -> i.getAmount() != null ? i.getAmount() : 0.0)
                    .sum();
            report.setTotalIncome(total);

            // Calculate income by category
            Map<String, Double> byCategory = new HashMap<>();
            for (IncomeRecord income : incomeRecords) {
                String source = income.getSource() != null ? income.getSource() : "OTHER";
                byCategory.put(source, byCategory.getOrDefault(source, 0.0) + income.getAmount());
            }
            report.setIncomeByCategory(byCategory);

            // Specific income types
            report.setRentIncome(byCategory.getOrDefault("RENT", 0.0));
            report.setDepositIncome(byCategory.getOrDefault("DEPOSIT", 0.0));
            report.setOtherIncome(byCategory.getOrDefault("OTHER", 0.0));
        }
        return this;
    }

    /**
     * Include expense analysis in the report
     */
    public FinancialReportBuilder includeExpenses() {
        if (expenses != null) {
            // Calculate total expenses
            Double total = expenses.stream()
                    .mapToDouble(e -> e.getAmount() != null ? e.getAmount() : 0.0)
                    .sum();
            report.setTotalExpenses(total);

            // Calculate expenses by category
            Map<String, Double> byCategory = new HashMap<>();
            for (Expense expense : expenses) {
                String type = expense.getType() != null ? expense.getType() : "OTHER";
                byCategory.put(type, byCategory.getOrDefault(type, 0.0) + expense.getAmount());
            }
            report.setExpenseByCategory(byCategory);
        }
        return this;
    }

    /**
     * Calculate profit/loss
     */
    public FinancialReportBuilder withProfitCalculation() {
        Double income = report.getTotalIncome() != null ? report.getTotalIncome() : 0.0;
        Double expenses = report.getTotalExpenses() != null ? report.getTotalExpenses() : 0.0;

        Double netProfit = income - expenses;
        report.setNetProfit(netProfit);
        report.setProfitMargin(report.calculateProfitMargin());

        return this;
    }

    /**
     * Include payment statistics
     */
    public FinancialReportBuilder withPaymentStats(Integer total, Integer paid, Integer pending, Integer overdue) {
        report.setTotalPayments(total);
        report.setPaidPayments(paid);
        report.setPendingPayments(pending);
        report.setOverduePayments(overdue);
        return this;
    }

    /**
     * Include property statistics
     */
    public FinancialReportBuilder withPropertyStats(Integer totalRooms, Integer occupiedRooms) {
        report.setTotalRooms(totalRooms);
        report.setOccupiedRooms(occupiedRooms);

        Double occupancyRate = totalRooms > 0
                ? (occupiedRooms.doubleValue() / totalRooms.doubleValue()) * 100
                : 0.0;
        report.setOccupancyRate(occupancyRate);

        return this;
    }

    /**
     * Build and return the final report
     */
    public FinancialReportDTO build() {
        return report;
    }
}
