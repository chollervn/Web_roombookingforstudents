package com.ecom.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Payment information
 * Decouples presentation layer from domain entities
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDTO {

    private Integer id;
    private Integer month;
    private Integer year;
    private Double rentAmount;
    private Double electricityUsage;
    private Double electricityCost;
    private Double electricityAmount;
    private Double waterUsage;
    private Double waterCost;
    private Double waterAmount;
    private Double internetAmount;
    private Double additionalFees;
    private Double totalAmount;
    private Double paidAmount;
    private String status; // PENDING, PAID, OVERDUE, PARTIAL
    private LocalDate dueDate;
    private LocalDate paidDate;
    private String note;

    // Room information
    private Integer roomId;
    private String roomName;

    // Booking information
    private Integer bookingId;

    // Tenant information
    private Integer tenantId;
    private String tenantName;

    /**
     * Calculate remaining balance
     */
    public Double getRemainingBalance() {
        if (totalAmount == null || paidAmount == null) {
            return totalAmount;
        }
        return totalAmount - paidAmount;
    }

    /**
     * Check if payment is overdue
     */
    public boolean isOverdue() {
        return "OVERDUE".equals(status) ||
                (dueDate != null && dueDate.isBefore(LocalDate.now()) && !"PAID".equals(status));
    }

    /**
     * Check if payment is pending
     */
    public boolean isPending() {
        return "PENDING".equals(status);
    }

    /**
     * Check if payment is fully paid
     */
    public boolean isPaid() {
        return "PAID".equals(status);
    }
}
