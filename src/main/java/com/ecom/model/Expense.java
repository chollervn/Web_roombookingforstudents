package com.ecom.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity representing expenses incurred by landlords
 * Categories: MAINTENANCE, UTILITIES, REPAIRS, TAXES, INSURANCE, OTHER
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String type; // MAINTENANCE, UTILITIES, REPAIRS, TAXES, INSURANCE, OTHER

    @Column(length = 100)
    private String category; // Sub-category within type

    private Double amount;

    private LocalDate expenseDate;

    @ManyToOne
    private Room room; // Optional: expense may be linked to a specific room

    @Column(length = 1000)
    private String description;

    private String receiptImage; // Path to receipt image

    private String paymentMethod; // CASH, BANK_TRANSFER, CREDIT_CARD

    @Column(length = 200)
    private String vendor; // Who was paid

    private Integer ownerId; // Owner who incurred this expense

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.expenseDate == null) {
            this.expenseDate = LocalDate.now();
        }
    }

    /**
     * Check if expense has a receipt
     */
    public boolean hasReceipt() {
        return receiptImage != null && !receiptImage.isEmpty();
    }

    /**
     * Get expense type display name
     */
    public String getTypeDisplayName() {
        if (type == null) {
            return "Unknown";
        }
        return switch (type) {
            case "MAINTENANCE" -> "Bảo trì";
            case "UTILITIES" -> "Tiện ích";
            case "REPAIRS" -> "Sửa chữa";
            case "TAXES" -> "Thuế";
            case "INSURANCE" -> "Bảo hiểm";
            default -> "Khác";
        };
    }
}
