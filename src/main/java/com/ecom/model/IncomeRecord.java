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
 * Entity representing income records for landlords
 * Auto-generated from MonthlyPayment when paid
 * Categories: RENT, DEPOSIT, UTILITIES, FEES, OTHER
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class IncomeRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String source; // RENT, DEPOSIT, UTILITIES, FEES, OTHER

    private Double amount;

    private LocalDate incomeDate;

    @ManyToOne
    private Room room;

    @ManyToOne
    private MonthlyPayment payment; // Link to payment if income is from payment

    @Column(length = 500)
    private String description;

    private Integer ownerId;

    private String category; // Sub-category

    private LocalDateTime createdDate;

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        if (this.incomeDate == null) {
            this.incomeDate = LocalDate.now();
        }
    }

    /**
     * Get income source display name
     */
    public String getSourceDisplayName() {
        if (source == null) {
            return "Unknown";
        }
        return switch (source) {
            case "RENT" -> "Tiền thuê";
            case "DEPOSIT" -> "Tiền cọc";
            case "UTILITIES" -> "Tiện ích";
            case "FEES" -> "Phí dịch vụ";
            default -> "Khác";
        };
    }
}
