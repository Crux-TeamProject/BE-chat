package com.project.crux.chat.config.handler;

import com.project.crux.chat.model.ChatMessage;
import com.project.crux.chat.repo.RedisChatRoomRepository;
import com.project.crux.chat.service.ChatService;
import com.project.crux.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class StompHandler implements ChannelInterceptor {
    private static final String SIMP_DESTINATION = "simpDestination";
    private static final String SIMP_SESSION_ID = "simpSessionId";
    private static final String SIMP_USER = "simpUser";
    private static final String AUTHORIZATION_HEADER = "Authorization";

    private final TokenProvider jwtTokenProvider;
    private final RedisChatRoomRepository redisChatRoomRepository;
    private final ChatService chatService;

    // websocket을 통해 들어온 요청이 처리 되기전 실행된다.
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (StompCommand.CONNECT == accessor.getCommand()) {
            validateToken(accessor);
        } else if (StompCommand.SUBSCRIBE == accessor.getCommand()) {
            String roomId = chatService.getRoomId(message.getHeaders().get(SIMP_DESTINATION, String.class));
            String sessionId = message.getHeaders().get(SIMP_SESSION_ID, String.class);
            String token = validateToken(accessor);
            String nickname = jwtTokenProvider.getNickname(token);

            redisChatRoomRepository.enterChatRoom(roomId, sessionId, nickname);
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.ENTER).roomId(roomId).sender(nickname).build());
            log.info("SUBSCRIBED {}, {}", nickname, roomId);
        } else if (StompCommand.DISCONNECT == accessor.getCommand()) {
            String sessionId = message.getHeaders().get(SIMP_SESSION_ID, String.class);
            String roomId = redisChatRoomRepository.getUserEnterRoomId(sessionId);
            String token = validateToken(accessor);
            String nickname = jwtTokenProvider.getNickname(token);
            chatService.sendChatMessage(ChatMessage.builder().type(ChatMessage.MessageType.QUIT).roomId(roomId).sender(nickname).build());
            redisChatRoomRepository.removeUserEnterInfo(sessionId, roomId);
            log.info("DISCONNECTED {}, {}", nickname, roomId);
        }
        return message;
    }

    private String validateToken(StompHeaderAccessor accessor) {
        String bearerToken = accessor.getFirstNativeHeader(AUTHORIZATION_HEADER);
        String token = jwtTokenProvider.extractToken(bearerToken);
        jwtTokenProvider.validateToken(token);
        return token;
    }
}
