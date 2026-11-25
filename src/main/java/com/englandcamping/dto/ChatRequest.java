package com.englandcamping.dto;

import java.util.List;

/**
 * 챗봇 질문 (손님 브라우저 → POST /api/chat 의 JSON) 예: {"message":"그럼 나가는
 * 건요?","history":[{"role":"user","text":"체크인 몇
 * 시예요?"},{"role":"model","text":"오후 1시예요."}]} history = 앞 대화 (브라우저가 들고 있다가 같이
 * 보냄, 서버는 저장하지 않음)
 */
public record ChatRequest(String message, List<ChatMessage> history) {

	/** 앞 대화 한 줄. role: "user"(손님) / "model"(챗봇) */
	public record ChatMessage(String role, String text) {
	}
}