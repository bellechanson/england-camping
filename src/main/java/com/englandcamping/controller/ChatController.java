package com.englandcamping.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.englandcamping.ai.ChatService;
import com.englandcamping.dto.ChatRequest;
import com.englandcamping.dto.ChatResponse;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 손님 챗봇 창구: POST /api/chat (로그인 없이 누구나) 받는 것: {"message":"질문","history":[...]}
 * → 돌려주는 것: {"ok":true,"reply":"답","remaining":19} 안전장치는 ChatService 에 (글자 수,
 * 하루 횟수, 실패 안내)
 */
@RestController
public class ChatController {

	/** 받는 글 전체 크기 상한 (질문 500자 + 앞 대화 6줄이면 충분. 이상하게 큰 요청은 거절) */
	private static final long MAX_BODY_BYTES = 20_000;

	private final ChatService chatService;

	public ChatController(ChatService chatService) {
		this.chatService = chatService;
	}

	@PostMapping("/api/chat")
	public ChatResponse chat(@RequestBody(required = false) ChatRequest request, HttpServletRequest http) {
		if (http.getContentLengthLong() > MAX_BODY_BYTES) {
			return ChatResponse.fail("질문이 너무 길어요. 짧게 나눠서 물어봐 주세요.", -1);
		}
		// 접속 주소(IP): 서버에서는 nginx 뒤라서 application-prod 의 forward-headers 설정으로 진짜 손님 주소가
		// 들어옴
		return chatService.answer(http.getRemoteAddr(), request);
	}
}