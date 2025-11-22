package com.ecom.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ecom.model.ChatMessage;
import com.ecom.model.Room;
import com.ecom.model.UserDtls;
import com.ecom.service.ChatService;
import com.ecom.service.RoomService;
import com.ecom.service.UserService;

@Controller
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * WebSocket endpoint: Send chat message
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessage message, Principal principal) {
        // Get sender from principal
        String email = principal.getName();
        UserDtls sender = userService.getUserByEmail(email);
        message.setSender(sender);

        // Save message to database
        ChatMessage savedMessage = chatService.saveMessage(message);

        // Send message to receiver's queue
        messagingTemplate.convertAndSendToUser(
                message.getReceiver().getId().toString(),
                "/queue/messages",
                savedMessage);

        // Also send to room topic if room is specified
        if (message.getRoom() != null) {
            messagingTemplate.convertAndSend(
                    "/topic/room/" + message.getRoom().getId(),
                    savedMessage);
        }
    }

    /**
     * WebSocket endpoint: Typing indicator
     */
    @MessageMapping("/chat.typing")
    @SendTo("/topic/typing")
    public ChatMessage sendTypingIndicator(@Payload ChatMessage message) {
        return message;
    }

    /**
     * REST endpoint: Get chat history for a room
     */
    @GetMapping("/chat/history/{roomId}")
    @ResponseBody
    public List<ChatMessage> getChatHistory(@PathVariable Integer roomId) {
        return chatService.getRoomChatHistory(roomId);
    }

    /**
     * REST endpoint: Get unread message count
     */
    @GetMapping("/chat/unread")
    @ResponseBody
    public Long getUnreadCount(Principal principal) {
        String email = principal.getName();
        UserDtls user = userService.getUserByEmail(email);
        return chatService.getUnreadCount(user.getId());
    }

    /**
     * REST endpoint: Mark messages as read
     */
    @PostMapping("/chat/read/{senderId}")
    @ResponseBody
    public void markAsRead(@PathVariable Integer senderId, Principal principal) {
        String email = principal.getName();
        UserDtls user = userService.getUserByEmail(email);
        chatService.markMessagesAsRead(user.getId(), senderId);
    }

    /**
     * Page: Admin chat interface
     */
    @GetMapping("/admin/chat")
    public String adminChat(Model m, Principal p) {
        String email = p.getName();
        UserDtls user = userService.getUserByEmail(email);

        // Get all rooms owned by this user
        List<Room> ownedRooms = roomService.getRoomsByOwnerId(user.getId());

        // Get conversation rooms with messages
        List<Room> conversationRooms = chatService.getConversationRooms(user.getId());

        m.addAttribute("ownedRooms", ownedRooms);
        m.addAttribute("conversationRooms", conversationRooms);
        m.addAttribute("unreadCount", chatService.getUnreadCount(user.getId()));

        return "admin/admin_chat";
    }

    /**
     * Page: User chat interface
     */
    /**
     * Page: User chat interface
     */
    @GetMapping("/user/chat")
    public String userChat(@RequestParam(required = false) Integer rid, Model m, Principal p) {
        String email = p.getName();
        UserDtls user = userService.getUserByEmail(email);

        // Get conversation rooms
        List<Room> conversationRooms = chatService.getConversationRooms(user.getId());

        if (rid != null) {
            Room room = roomService.getRoomById(rid);
            if (room != null) {
                m.addAttribute("room", room);
                // If this room is not in conversation list, add it temporarily or handle in UI
                boolean exists = conversationRooms.stream().anyMatch(r -> r.getId().equals(rid));
                if (!exists) {
                    conversationRooms.add(0, room);
                }
            }
        }

        m.addAttribute("conversationRooms", conversationRooms);
        m.addAttribute("unreadCount", chatService.getUnreadCount(user.getId()));

        return "user/chat";
    }
}
