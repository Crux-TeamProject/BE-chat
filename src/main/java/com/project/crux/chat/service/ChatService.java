package com.project.crux.chat.service;

import com.project.crux.chat.model.ChatMessage;
import com.project.crux.chat.repo.RedisChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ChatService {

    private final ChannelTopic channelTopic;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisChatRoomRepository redisChatRoomRepository;

    /**
     * destination정보에서 roomId 추출
     */
    public String getRoomId(String destination) {
        if (destination == null) {
            return "";
        }
        int lastIndex = destination.lastIndexOf('/');
        if (lastIndex != -1)
            return destination.substring(lastIndex + 1);
        else
            return "";
    }

    /**
     * 채팅방에 메시지 발송
     */
    public void sendChatMessage(ChatMessage chatMessage) {
        LocalDateTime createdAt = LocalDateTime.now();
        chatMessage.setCreatedAt(createdAt);
        chatMessage.updateUserList(redisChatRoomRepository.getUsers(chatMessage.getRoomId()));
        redisChatRoomRepository.save(chatMessage);
        redisTemplate.convertAndSend(channelTopic.getTopic(), chatMessage);
    }

    public List<ChatMessage> getMessages(String roomId) {
        //redis에 저장되어있는 message 들 출력
        return redisChatRoomRepository.findAllMessage(roomId);
    }
}
