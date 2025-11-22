package com.ecom.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Data Transfer Object for Expense information
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseDTO {

    private Integer id;
    private String type; // MAINTENANCE, UTILITIES, REPAIRS, TAXES, INSURANCE, OTHER
    private String category;
    private Double amount;
    private LocalDate expenseDate;
    private Integer roomId;
    private String roomName;
    private String description;
    private String receiptImage;
    private String paymentMethod;
    private String vendor;

    /**
     * Get badge color based on expense type
     */
    public String getTypeBadgeColor() {
        if (type == null) {
            return "secondary";
        }
        return switch (type) {
            case "MAINTENANCE" -> "primary";
            case "UTILITIES" -> "info";
            case "REPAIRS" -> "warning";
            case "TAXES" -> "danger";
            case "INSURANCE" -> "success";
            default -> "secondary";
        };
    }
}
