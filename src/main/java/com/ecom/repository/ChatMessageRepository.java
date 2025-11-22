package com.ecom.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.ecom.model.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {

    // Get chat history for a specific room
    List<ChatMessage> findByRoomIdOrderByTimestampAsc(Integer roomId);

    // Get chat history between two users
    @Query("SELECT m FROM ChatMessage m WHERE (m.sender.id = :userId1 AND m.receiver.id = :userId2) " +
            "OR (m.sender.id = :userId2 AND m.receiver.id = :userId1) ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatBetweenUsers(@Param("userId1") Integer userId1, @Param("userId2") Integer userId2);

    // Count unread messages for a user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Integer userId);

    // Get unread messages for a user
    List<ChatMessage> findByReceiverIdAndIsReadFalseOrderByTimestampDesc(Integer receiverId);

    // Get all conversations for a user (distinct room IDs)
    @Query("SELECT DISTINCT m.room FROM ChatMessage m WHERE m.sender.id = :userId OR m.receiver.id = :userId")
    List<com.ecom.model.Room> findConversationRoomsByUserId(@Param("userId") Integer userId);

    // Mark messages as read
    @Query("UPDATE ChatMessage m SET m.isRead = true WHERE m.receiver.id = :userId AND m.sender.id = :senderId AND m.isRead = false")
    void markMessagesAsRead(@Param("userId") Integer userId, @Param("senderId") Integer senderId);
}
