package com.englandcamping.ai;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import com.englandcamping.ai.GeminiClient.Turn;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.dto.ChatRequest;
import com.englandcamping.dto.ChatResponse;
import com.englandcamping.service.SiteSettingService;

/**
 * 챗봇 "지배인" — 질문 하나를 순서대로 처리 ① 질문 검사(빈칸·글자 수) ② 하루 횟수(전체·개인) ③ 업무 지시서 + 앞 대화 ④
 * Gemini 에 묻기 ⑤ 실패하면 상황에 맞는 안내 질문·답 내용은 저장하지도, 로그에 남기지도 않음 (오류 번호만 로그)
 */
@Service
public class ChatService {

	private static final Logger log = LoggerFactory.getLogger(ChatService.class);

	/** 앞 대화 한 줄의 최대 글자 수 (브라우저가 이상하게 긴 글을 보내도 잘라냄) */
	private static final int MAX_HISTORY_TEXT = 1000;

	private final GeminiClient gemini;
	private final CampInfoPromptBuilder promptBuilder;
	private final ChatRateLimiter limiter;
	private final SiteSettingService settings;
	private final int maxMessageLength;
	private final int historySize;

	public ChatService(GeminiClient gemini, CampInfoPromptBuilder promptBuilder, ChatRateLimiter limiter,
			SiteSettingService settings, @Value("${chatbot.max-message-length:500}") int maxMessageLength,
			@Value("${chatbot.history-size:6}") int historySize) {
		this.gemini = gemini;
		this.promptBuilder = promptBuilder;
		this.limiter = limiter;
		this.settings = settings;
		this.maxMessageLength = maxMessageLength;
		this.historySize = historySize;
	}

	/** 손님 질문 하나 처리. userKey = 접속 주소(IP) */
	public ChatResponse answer(String userKey, ChatRequest request) {
		String phone = settings.get(SettingKey.PHONE);

		// ① 질문 검사
		String message = (request == null || request.message() == null) ? "" : request.message().strip();
		if (message.isEmpty()) {
			return ChatResponse.fail("궁금한 점을 입력해 주세요.", -1);
		}
		if (message.length() > maxMessageLength) {
			return ChatResponse.fail("질문은 " + maxMessageLength + "자 이내로 적어 주세요.", -1);
		}
		// 키가 없거나, 관리자가 꺼 둔 상태(영국이 자는 중)면 구글에 묻지 않음
		if (!gemini.hasKey() || !settings.isChatbotAwake()) {
			return ChatResponse.fail("지금은 영국이를 이용할 수 없습니다. 궁금한 점은 전화(" + phone + ")로 문의해 주세요.", -1);
		}

		// ② 하루 횟수 (구글에 묻기 전에 셈 → 장난으로 계속 보내도 한도 이상은 구글로 안 감)
		int remaining = limiter.tryUse(userKey);
		if (remaining == ChatRateLimiter.TOTAL_EXCEEDED) {
			return totalExceeded(phone);
		}
		if (remaining == ChatRateLimiter.USER_EXCEEDED) {
			return ChatResponse.fail("오늘 질문할 수 있는 횟수(" + limiter.getDailyLimitPerUser() + "번)를 모두 쓰셨어요. "
					+ "더 궁금한 점은 전화(" + phone + ")로 문의해 주세요.", 0);
		}

		// ③ 업무 지시서 + 앞 대화
		String systemPrompt = promptBuilder.build();
		List<Turn> turns = buildTurns(request.history(), message);

		// ④ Gemini 에 묻기
		try {
			String reply = gemini.generate(systemPrompt, turns);
			if (!reply.isBlank()) {
				return ChatResponse.ok(reply, remaining);
			}
			log.warn("챗봇: 빈 답을 받음");
		} catch (RestClientResponseException e) {
			int status = e.getStatusCode().value();
			if (status == 429) {
				// 429 = 한도 초과. 구글 설명에 "PerDay"가 있으면 하루치, 아니면 1분치(손님이 몰림)
				if (e.getResponseBodyAsString().contains("PerDay")) {
					log.warn("챗봇: 구글 하루 무료 한도 다 참 → 오늘은 종료");
					limiter.markTotalExhausted();
					return totalExceeded(phone);
				}
				log.warn("챗봇: 1분 한도 초과 (손님이 몰림)");
				return ChatResponse.fail("지금 질문이 몰려서 답하기 어려워요. 1분 정도 뒤에 다시 물어봐 주세요.", remaining);
			}
			// 400·403 = 키 문제, 404 = 모델 이름 틀림, 5xx = 구글 쪽 고장
			log.warn("챗봇: 구글 오류 (HTTP {})", status);
		} catch (RestClientException e) {
			log.warn("챗봇: 구글 연결 실패 ({})", e.getClass().getSimpleName());
		}

		// ⑤ 그 밖의 실패
		return ChatResponse.fail("지금은 답하기 어려워요. 잠시 후 다시 물어봐 주시거나 전화(" + phone + ")로 문의해 주세요.", remaining);
	}

	/** 전체 하루 한도가 다 찼을 때 안내 */
	private static ChatResponse totalExceeded(String phone) {
		return ChatResponse.fail("오늘 상담 도우미가 받을 수 있는 질문을 모두 채웠어요. " + "내일 다시 이용해 주시거나 전화(" + phone + ")로 문의해 주세요.", -1);
	}

	/**
	 * 앞 대화 + 이번 질문 → 구글에 보낼 대화 목록 - 최근 historySize 줄만, role 은 user/model 만, 빈 줄 빼고,
	 * 너무 긴 글은 자름 - 맨 앞은 손님(user)이어야 해서 앞쪽 model 줄은 버림 - 같은 쪽이 두 번 연속이면 한 줄로 합침 (구글은
	 * user/model 이 번갈아 오는 걸 기대함)
	 */
	private List<Turn> buildTurns(List<ChatRequest.ChatMessage> history, String message) {
		List<Turn> turns = new ArrayList<>();
		if (history != null) {
			int from = Math.max(0, history.size() - historySize);
			for (ChatRequest.ChatMessage m : history.subList(from, history.size())) {
				if (m == null || m.text() == null || m.text().isBlank()) {
					continue;
				}
				String role = m.role();
				if (!"user".equals(role) && !"model".equals(role)) {
					continue;
				}
				String text = m.text().strip();
				if (text.length() > MAX_HISTORY_TEXT) {
					text = text.substring(0, MAX_HISTORY_TEXT);
				}
				addTurn(turns, new Turn(role, text));
			}
		}
		addTurn(turns, Turn.user(message));
		while (!turns.isEmpty() && "model".equals(turns.get(0).role())) {
			turns.remove(0);
		}
		return turns;
	}

	/** 한 줄 추가. 앞 줄과 같은 쪽이면 합침 */
	private static void addTurn(List<Turn> turns, Turn turn) {
		if (!turns.isEmpty()) {
			Turn last = turns.get(turns.size() - 1);
			if (last.role().equals(turn.role())) {
				turns.set(turns.size() - 1, new Turn(last.role(), last.text() + "\n" + turn.text()));
				return;
			}
		}
		turns.add(turn);
	}
}