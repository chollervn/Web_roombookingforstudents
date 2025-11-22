package com.ecom.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecom.model.ChatMessage;
import com.ecom.repository.ChatMessageRepository;
import com.ecom.service.ChatService;

@Service
public class ChatServiceImpl implements ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Override
    public ChatMessage saveMessage(ChatMessage message) {
        if (message.getTimestamp() == null) {
            message.setTimestamp(LocalDateTime.now());
        }
        if (message.getIsRead() == null) {
            message.setIsRead(false);
        }
        return chatMessageRepository.save(message);
    }

    @Override
    public List<ChatMessage> getRoomChatHistory(Integer roomId) {
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId);
    }

    @Override
    public List<ChatMessage> getChatBetweenUsers(Integer userId1, Integer userId2) {
        return chatMessageRepository.findChatBetweenUsers(userId1, userId2);
    }

    @Override
    public Long getUnreadCount(Integer userId) {
        return chatMessageRepository.countUnreadMessages(userId);
    }

    @Override
    public List<ChatMessage> getUnreadMessages(Integer userId) {
        return chatMessageRepository.findByReceiverIdAndIsReadFalseOrderByTimestampDesc(userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Integer userId, Integer senderId) {
        chatMessageRepository.markMessagesAsRead(userId, senderId);
    }

    @Override
    public List<com.ecom.model.Room> getConversationRooms(Integer userId) {
        return chatMessageRepository.findConversationRoomsByUserId(userId);
    }
}
