package com.ecom.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private UserDtls sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private UserDtls receiver;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    private String message;

    private LocalDateTime timestamp;

    private Boolean isRead = false;

    // Helper method to check if message is from owner
    public boolean isFromOwner() {
        return sender != null && "ROLE_OWNER".equals(sender.getRole());
    }
}
