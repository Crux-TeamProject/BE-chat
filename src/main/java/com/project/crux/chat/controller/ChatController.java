package com.project.crux.chat.controller;

import com.project.crux.chat.model.ChatMessage;
import com.project.crux.chat.service.ChatService;
import com.project.crux.common.ResponseDto;
import com.project.crux.security.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Controller
public class ChatController {
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private final TokenProvider jwtTokenProvider;
    private final ChatService chatService;

    /**
     * websocket "/pub/chat/message"로 들어오는 메시지를 처리한다.
     */
    @MessageMapping("/chat/message")
    public void message(ChatMessage message, @Header(AUTHORIZATION_HEADER) String bearerToken) {
        String token = jwtTokenProvider.extractToken(bearerToken);
        String nickname = jwtTokenProvider.getNickname(token);
        String imgUrl = jwtTokenProvider.getImgUrl(token);
        message.setSender(nickname);
        message.setImgUrl(imgUrl);
        message.setType(ChatMessage.MessageType.TALK);
        chatService.sendChatMessage(message);
    }

    //이전 채팅 기록 조회
    @GetMapping("/chat/messages/{roomId}")
    @ResponseBody
    public ResponseDto<List<ChatMessage>> getMessage(@PathVariable String roomId) {
        return ResponseDto.success(chatService.getMessages(roomId));
    }
}
