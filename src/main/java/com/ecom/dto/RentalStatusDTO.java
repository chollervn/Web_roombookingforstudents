package com.ecom.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Rental Status information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RentalStatusDTO {

    private Integer bookingId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer durationMonths;
    private String status; // ACTIVE, CANCELLED, EXPIRED
    private Double monthlyRent;
    private Double depositAmount;
    private String paymentMethod;
    private String note;

    /**
     * Get status badge color for UI
     */
    public String getStatusBadgeColor() {
        if (status == null) {
            return "secondary";
        }
        return switch (status) {
            case "ACTIVE" -> "success";
            case "CANCELLED" -> "danger";
            case "EXPIRED" -> "warning";
            default -> "secondary";
        };
    }

    /**
     * Check if rental is currently active
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    /**
     * Get remaining days of rental
     */
    public long getRemainingDays() {
        if (endDate == null) {
            return -1;
        }
        return java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), endDate);
    }
}
