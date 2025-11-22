package com.ecom.service;

import java.util.List;

import com.ecom.model.ChatMessage;

public interface ChatService {

    // Save a chat message
    ChatMessage saveMessage(ChatMessage message);

    // Get chat history for a specific room
    List<ChatMessage> getRoomChatHistory(Integer roomId);

    // Get chat history between two users
    List<ChatMessage> getChatBetweenUsers(Integer userId1, Integer userId2);

    // Get unread messages count for a user
    Long getUnreadCount(Integer userId);

    // Get unread messages for a user
    List<ChatMessage> getUnreadMessages(Integer userId);

    // Mark messages as read
    void markMessagesAsRead(Integer userId, Integer senderId);

    // Get all conversation rooms for a user
    List<com.ecom.model.Room> getConversationRooms(Integer userId);
}
