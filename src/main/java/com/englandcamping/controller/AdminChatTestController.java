package com.englandcamping.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.englandcamping.ai.CampInfoPromptBuilder;
import com.englandcamping.ai.GeminiClient;

/**
 * [시험용 — 챗봇 완성 후 삭제 예정] 관리자로 로그인해야 열림 (SecurityConfig 의 /admin/** 규칙) -
 * /admin/chatbot/test : 키·모델 목록·"안녕?" 확인 (2단계) - /admin/chatbot/prompt : 업무 지시서
 * 보기 + 질문해 보기 (3단계). ?q=질문 으로 바꿔 물어볼 수 있음 주의: 질문을 보낼 때마다 하루 무료 한도(500번) 중 1번을 씀
 */
@RestController
public class AdminChatTestController {

	private final GeminiClient gemini;
	private final CampInfoPromptBuilder promptBuilder;

	public AdminChatTestController(GeminiClient gemini, CampInfoPromptBuilder promptBuilder) {
		this.gemini = gemini;
		this.promptBuilder = promptBuilder;
	}

	@GetMapping(value = "/admin/chatbot/test", produces = "text/plain;charset=UTF-8")
	public String test() {
		StringBuilder out = new StringBuilder();

		// ① 키 확인 (값은 보여 주지 않음)
		out.append("① API 키: ");
		if (!gemini.hasKey()) {
			out.append("없음 → Windows 환경 변수 GEMINI_API_KEY 확인 후 STS 완전히 껐다 켜기\n");
			return out.toString();
		}
		out.append("있음\n");

		// ② 쓸 수 있는 모델 ID 목록
		try {
			List<String> ids = gemini.listModels();
			out.append("\n② 이 키로 쓸 수 있는 모델 ID (").append(ids.size()).append("개)\n");
			ids.forEach(id -> out.append("   - ").append(id).append('\n'));
		} catch (RestClientResponseException e) {
			out.append("\n② 모델 목록 실패: ").append(e.getStatusCode()).append('\n').append(e.getResponseBodyAsString())
					.append('\n');
		} catch (RestClientException e) {
			out.append("\n② 구글에 연결 못 함: ").append(e.getMessage()).append('\n');
		}

		// ③ 진짜 질문 하나 보내 보기
		out.append("\n③ 설정한 모델: ").append(gemini.getModel()).append('\n');
		try {
			long start = System.currentTimeMillis();
			String reply = gemini.ask("안녕? 한국어로 한 문장으로만 인사해 줘.");
			long ms = System.currentTimeMillis() - start;
			out.append("   답 (").append(ms).append("ms 걸림): ").append(reply.isEmpty() ? "(빈 답)" : reply).append('\n');
		} catch (RestClientResponseException e) {
			// 404 = 모델 ID가 틀림 / 429 = 한도 초과 / 400·403 = 키 문제
			out.append("   실패: ").append(e.getStatusCode()).append('\n').append(e.getResponseBodyAsString())
					.append('\n');
		} catch (RestClientException e) {
			out.append("   구글에 연결 못 함: ").append(e.getMessage()).append('\n');
		}
		return out.toString();
	}

	/** 업무 지시서 + 질문 시험. 예: /admin/chatbot/prompt?q=애견 동반 되나요? */
	@GetMapping(value = "/admin/chatbot/prompt", produces = "text/plain;charset=UTF-8")
	public String prompt(@RequestParam(defaultValue = "체크인은 몇 시예요? 그리고 1박 요금도 알려 주세요.") String q) {
		String systemPrompt = promptBuilder.build();
		StringBuilder out = new StringBuilder();

		out.append("■ 질문: ").append(q).append('\n');
		try {
			long start = System.currentTimeMillis();
			String reply = gemini.ask(systemPrompt, q);
			long ms = System.currentTimeMillis() - start;
			out.append("■ 답 (").append(ms).append("ms): \n").append(reply.isEmpty() ? "(빈 답)" : reply).append('\n');
		} catch (RestClientResponseException e) {
			out.append("■ 실패: ").append(e.getStatusCode()).append('\n').append(e.getResponseBodyAsString())
					.append('\n');
		} catch (RestClientException e) {
			out.append("■ 구글에 연결 못 함: ").append(e.getMessage()).append('\n');
		}

		out.append("\n\n==================== 아래는 AI에게 함께 보낸 업무 지시서 (").append(systemPrompt.length())
				.append("자) ====================\n\n").append(systemPrompt);
		return out.toString();
	}
}