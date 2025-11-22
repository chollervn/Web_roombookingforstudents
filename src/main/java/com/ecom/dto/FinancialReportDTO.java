package com.ecom.dto;

import java.time.LocalDate;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Financial Reports
 * Aggregates income and expense data for reporting
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialReportDTO {

    private Integer ownerId;
    private LocalDate startDate;
    private LocalDate endDate;

    // Income summary
    private Double totalIncome;
    private Double rentIncome;
    private Double depositIncome;
    private Double otherIncome;
    private Map<String, Double> incomeByCategory;
    private Map<String, Double> incomeByMonth;

    // Expense summary
    private Double totalExpenses;
    private Map<String, Double> expenseByCategory;
    private Map<String, Double> expenseByMonth;

    // Profit/Loss
    private Double netProfit;
    private Double profitMargin;

    // Statistics
    private Integer totalPayments;
    private Integer paidPayments;
    private Integer pendingPayments;
    private Integer overduePayments;

    // Property statistics
    private Integer totalRooms;
    private Integer occupiedRooms;
    private Double occupancyRate;

    /**
     * Calculate profit margin percentage
     */
    public Double calculateProfitMargin() {
        if (totalIncome == null || totalIncome == 0) {
            return 0.0;
        }
        return (netProfit / totalIncome) * 100;
    }

    /**
     * Check if report shows profit
     */
    public boolean isProfitable() {
        return netProfit != null && netProfit > 0;
    }

    /**
     * Get status color for UI display
     */
    public String getStatusColor() {
        if (netProfit == null) {
            return "secondary";
        }
        return netProfit >= 0 ? "success" : "danger";
    }
}
