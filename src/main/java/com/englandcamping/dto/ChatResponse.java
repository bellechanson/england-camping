package com.englandcamping.dto;

/**
 * 챗봇 답 (서버 → 손님 브라우저 JSON) 예: {"ok":true,"reply":"체크아웃은 낮
 * 12시예요.","remaining":18} - ok: AI가 제대로 답했는지 (false면 reply 에 안내 문구 — 한도 초과, 오류
 * 등) - remaining: 이 손님의 오늘 남은 질문 수 (-1 = 해당 없음)
 */
public record ChatResponse(boolean ok, String reply, int remaining) {

	public static ChatResponse ok(String reply, int remaining) {
		return new ChatResponse(true, reply, remaining);
	}

	public static ChatResponse fail(String reply, int remaining) {
		return new ChatResponse(false, reply, remaining);
	}
}