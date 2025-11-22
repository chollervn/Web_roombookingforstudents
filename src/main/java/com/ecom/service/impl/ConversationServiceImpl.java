package com.ecom.service.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ecom.exception.BusinessException;
import com.ecom.model.Conversation;
import com.ecom.model.RoomBooking;
import com.ecom.repository.ConversationRepository;
import com.ecom.repository.RoomBookingRepository;
import com.ecom.repository.UserRepository;
import com.ecom.service.ConversationService;

/**
 * Implementation of ConversationService
 * Follows Single Responsibility - only handles conversation operations
 */
@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private ConversationRepository conversationRepository;

    @Autowired
    private RoomBookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public Conversation getOrCreateConversation(Integer tenantId, Integer landlordId, Integer bookingId) {
        // Try to find existing conversation
        Conversation existing = conversationRepository.findByTenantAndLandlordAndBooking(
                tenantId, landlordId, bookingId);

        if (existing != null) {
            return existing;
        }

        // Create new conversation
        RoomBooking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> BusinessException.notFound("Booking", bookingId));

        Conversation conversation = new Conversation();
        conversation.setTenant(userRepository.findById(tenantId).orElse(null));
        conversation.setLandlord(userRepository.findById(landlordId).orElse(null));
        conversation.setRoomBooking(booking);
        conversation.setRoom(booking.getRoom());
        conversation.setSubject("Rental inquiry - " + booking.getRoom().getRoomName());

        return conversationRepository.save(conversation);
    }

    @Override
    public Conversation getConversationById(Integer id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("Conversation", id));
    }

    @Override
    public List<Conversation> getConversationsByTenant(Integer tenantId) {
        return conversationRepository.findByTenantIdOrderByLastMessageDateDesc(tenantId);
    }

    @Override
    public List<Conversation> getConversationsByLandlord(Integer landlordId) {
        return conversationRepository.findByLandlordIdOrderByLastMessageDateDesc(landlordId);
    }

    @Override
    public void markAsRead(Integer conversationId, Integer userId) {
        Conversation conversation = getConversationById(conversationId);
        conversation.markAsReadFor(userId);
        conversationRepository.save(conversation);
    }

    @Override
    public void incrementUnreadCount(Integer conversationId, Integer recipientId) {
        Conversation conversation = getConversationById(conversationId);
        conversation.incrementUnreadFor(recipientId);
        conversationRepository.save(conversation);
    }

    @Override
    public void closeConversation(Integer conversationId) {
        Conversation conversation = getConversationById(conversationId);
        conversation.setStatus("CLOSED");
        conversationRepository.save(conversation);
    }
}
