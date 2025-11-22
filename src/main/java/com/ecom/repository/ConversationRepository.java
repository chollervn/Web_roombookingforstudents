package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.Conversation;

/**
 * Repository for Conversation entity
 */
public interface ConversationRepository extends JpaRepository<Conversation, Integer> {
    
    // Find conversations by tenant
    List<Conversation> findByTenantIdOrderByLastMessageDateDesc(Integer tenantId);
    
    // Find conversations by landlord
    List<Conversation> findByLandlordIdOrderByLastMessageDateDesc(Integer landlordId);
    
    // Find conversation by room booking
    Conversation findByRoomBookingId(Integer roomBookingId);
    
    // Find conversations by room
    List<Conversation> findByRoomIdOrderByLastMessageDateDesc(Integer roomId);
    
    // Find active conversations for landlord with unread messages
    @Query("SELECT c FROM Conversation c WHERE c.landlord.id = :landlordId AND c.status IN ('OPEN', 'AWAITING_RESPONSE') AND c.unreadLandlordCount > 0 ORDER BY c.lastMessageDate DESC")
    List<Conversation> findActiveLandlordConversationsWithUnread(@Param("landlordId") Integer landlordId);
    
    // Find active conversations for tenant with unread messages
    @Query("SELECT c FROM Conversation c WHERE c.tenant.id = :tenantId AND c.status IN ('OPEN', 'AWAITING_RESPONSE') AND c.unreadTenantCount > 0 ORDER BY c.lastMessageDate DESC")
    List<Conversation> findActiveTenantConversationsWithUnread(@Param("tenantId") Integer tenantId);
    
    // Find conversation between specific tenant and landlord for a booking
    @Query("SELECT c FROM Conversation c WHERE c.tenant.id = :tenantId AND c.landlord.id = :landlordId AND c.roomBooking.id = :bookingId")
    Conversation findByTenantAndLandlordAndBooking(@Param("tenantId") Integer tenantId, 
                                                   @Param("landlordId") Integer landlordId, 
                                                   @Param("bookingId") Integer bookingId);
}
