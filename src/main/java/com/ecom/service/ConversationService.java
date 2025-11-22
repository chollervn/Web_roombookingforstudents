package com.ecom.service;

import java.util.List;

import com.ecom.model.Conversation;

/**
 * Service interface for Conversation operations
 * Follows Interface Segregation Principle
 */
public interface ConversationService {

    /**
     * Get or create conversation for a booking
     */
    Conversation getOrCreateConversation(Integer tenantId, Integer landlordId, Integer bookingId);

    /**
     * Get conversation by ID
     */
    Conversation getConversationById(Integer id);

    /**
     * Get conversations for tenant
     */
    List<Conversation> getConversationsByTenant(Integer tenantId);

    /**
     * Get conversations for landlord
     */
    List<Conversation> getConversationsByLandlord(Integer landlordId);

    /**
     * Mark conversation as read for user
     */
    void markAsRead(Integer conversationId, Integer userId);

    /**
     * Increment unread count for recipient
     */
    void incrementUnreadCount(Integer conversationId, Integer recipientId);

    /**
     * Close a conversation
     */
    void closeConversation(Integer conversationId);
}
