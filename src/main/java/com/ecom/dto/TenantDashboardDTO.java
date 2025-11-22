package com.ecom.dto;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Aggregated data for Tenant Dashboard
 * Combines rental, payment, and communication information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantDashboardDTO {

    // Current rental information
    private RoomDTO currentRoom;
    private RentalStatusDTO rentalStatus;

    // Payment information
    private PaymentDTO nextPayment;
    private List<PaymentDTO> upcomingPayments;
    private List<PaymentDTO> paymentHistory;
    private Double totalPaid;
    private Double totalPending;

    // Landlord contact
    private LandlordContactDTO landlordContact;

    // Communication
    private Integer unreadMessages;
    private Integer conversationId;

    /**
     * Check if rental is expiring soon (within 30 days)
     */
    public boolean isExpiringSoon() {
        if (rentalStatus == null || rentalStatus.getEndDate() == null) {
            return false;
        }
        LocalDate thirtyDaysFromNow = LocalDate.now().plusDays(30);
        return rentalStatus.getEndDate().isBefore(thirtyDaysFromNow) &&
                rentalStatus.getEndDate().isAfter(LocalDate.now());
    }

    /**
     * Check if there are overdue payments
     */
    public boolean hasOverduePayments() {
        if (upcomingPayments == null) {
            return false;
        }
        return upcomingPayments.stream().anyMatch(PaymentDTO::isOverdue);
    }

    /**
     * Get days until next payment
     */
    public long getDaysUntilNextPayment() {
        if (nextPayment == null || nextPayment.getDueDate() == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), nextPayment.getDueDate());
    }
}
