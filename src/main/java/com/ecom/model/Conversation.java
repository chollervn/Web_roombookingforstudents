package com.ecom.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Enhanced Conversation entity for tenant-landlord communication
 * Links to specific RoomBooking for context
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    private UserDtls tenant;

    @ManyToOne
    private UserDtls landlord;

    @ManyToOne
    private RoomBooking roomBooking; // Context: which rental this conversation is about

    @ManyToOne
    private Room room; // Quick reference to room

    private String status; // OPEN, CLOSED, AWAITING_RESPONSE

    private String subject; // Conversation subject/title

    private LocalDateTime createdDate;

    private LocalDateTime lastMessageDate;

    private Integer unreadTenantCount; // Unread messages for tenant

    private Integer unreadLandlordCount; // Unread messages for landlord

    @PrePersist
    public void prePersist() {
        this.createdDate = LocalDateTime.now();
        this.lastMessageDate = LocalDateTime.now();
        if (this.status == null) {
            this.status = "OPEN";
        }
        if (this.unreadTenantCount == null) {
            this.unreadTenantCount = 0;
        }
        if (this.unreadLandlordCount == null) {
            this.unreadLandlordCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.lastMessageDate = LocalDateTime.now();
    }

    /**
     * Mark as read for a user
     */
    public void markAsReadFor(Integer userId) {
        if (tenant != null && tenant.getId().equals(userId)) {
            this.unreadTenantCount = 0;
        } else if (landlord != null && landlord.getId().equals(userId)) {
            this.unreadLandlordCount = 0;
        }
    }

    /**
     * Increment unread count for recipient
     */
    public void incrementUnreadFor(Integer recipientId) {
        if (tenant != null && tenant.getId().equals(recipientId)) {
            this.unreadTenantCount++;
        } else if (landlord != null && landlord.getId().equals(recipientId)) {
            this.unreadLandlordCount++;
        }
    }

    /**
     * Check if conversation is active
     */
    public boolean isActive() {
        return "OPEN".equals(status) || "AWAITING_RESPONSE".equals(status);
    }
}
