package com.englandcamping.ai;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 구글 Gemini API 와 이야기하는 "전화기". - 우리 서버 → 구글 서버로 HTTPS 요청을 보내고, 답(JSON)에서 글자만 꺼내
 * 돌려줌 - API 키는 주소(?key=...)가 아니라 요청 머리글(x-goog-api-key)에 넣음 → 주소가 로그에 찍혀도 키는 안
 * 남음 - 무료 등급: 결제 계정을 연결하지 않았으므로 한도를 넘으면 돈이 나가는 게 아니라 오류(429)가 옴
 */
@Component
public class GeminiClient {

	/** 구글 Gemini API 기본 주소 (고정 주소만 부름 → 손님이 넣은 주소를 서버가 대신 여는 일은 없음) */
	private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta";

	/** 구글이 돌려준 JSON 을 Map(이름표 → 값) 모양으로 받기 위한 표시 */
	private static final ParameterizedTypeReference<Map<String, Object>> JSON_MAP = new ParameterizedTypeReference<>() {
	};

	private final RestClient restClient;
	private final String apiKey;
	private final String model;

	public GeminiClient(@Value("${gemini.api-key:}") String apiKey, @Value("${gemini.model}") String model) {
		this.apiKey = apiKey == null ? "" : apiKey.strip();
		this.model = model;

		// 기다리는 시간 제한: 연결 5초, 답 20초 (구글이 느려도 우리 서버가 계속 붙잡혀 있지 않게)
		SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
		factory.setConnectTimeout(Duration.ofSeconds(5));
		factory.setReadTimeout(Duration.ofSeconds(20));

		this.restClient = RestClient.builder().baseUrl(BASE_URL).requestFactory(factory)
				.defaultHeader("x-goog-api-key", this.apiKey) // 모든 요청에 출입증(키)을 붙임
				.build();
	}

	/** 대화 한 줄. role 은 "user"(손님) 또는 "model"(AI) — 구글이 정한 이름 */
	public record Turn(String role, String text) {

		public static Turn user(String text) {
			return new Turn("user", text);
		}
	}

	/** 키가 들어 있는지 (값은 절대 밖으로 내보내지 않음) */
	public boolean hasKey() {
		return !apiKey.isEmpty();
	}

	/** 쓰는 모델 ID */
	public String getModel() {
		return model;
	}

	/**
	 * 이 키로 "글 만들기(generateContent)"가 되는 모델 ID 목록. 예: models/gemini-3.5-flash-lite →
	 * "gemini-3.5-flash-lite" 로 앞부분을 떼고 돌려줌
	 */
	public List<String> listModels() {
		Map<String, Object> response = restClient.get().uri("/models?pageSize=200").retrieve().body(JSON_MAP);

		List<String> ids = new ArrayList<>();
		if (response != null && response.get("models") instanceof List<?> models) {
			for (Object item : models) {
				if (item instanceof Map<?, ?> m && m.get("supportedGenerationMethods") instanceof List<?> methods
						&& methods.contains("generateContent")) {
					ids.add(String.valueOf(m.get("name")).replaceFirst("^models/", ""));
				}
			}
		}
		return ids;
	}

	/** 질문 하나 (지시서 없이) — 시험 화면용 */
	public String ask(String message) {
		return generate(null, List.of(Turn.user(message)));
	}

	/** 업무 지시서 + 질문 하나 — 시험 화면용 */
	public String ask(String systemPrompt, String message) {
		return generate(systemPrompt, List.of(Turn.user(message)));
	}

	/**
	 * 대화 보내고 답 받기 (챗봇이 쓰는 것)
	 * 
	 * @param systemPrompt 업무 지시서 (없으면 null)
	 * @param turns        대화 목록. 첫 줄은 손님(user), 마지막 줄은 이번 질문(user) 보내는 모양:
	 *                     {"systemInstruction":{...},
	 *                     "contents":[{"role":"user","parts":[{"text":"..."}]},
	 *                     ...], "generationConfig":{...}} - systemInstruction: 운영자
	 *                     지시 칸. 손님 말(contents)과 따로 가서, AI가 둘을 구분함 - temperature
	 *                     0.3: 답을 덜 들쭉날쭉하게 / maxOutputTokens 600: 답 길이 상한
	 */
	public String generate(String systemPrompt, List<Turn> turns) {
		List<Map<String, Object>> contents = new ArrayList<>();
		for (Turn turn : turns) {
			contents.add(Map.of("role", turn.role(), "parts", List.of(Map.of("text", turn.text()))));
		}

		Map<String, Object> body = new LinkedHashMap<>();
		if (systemPrompt != null && !systemPrompt.isBlank()) {
			body.put("systemInstruction", Map.of("parts", List.of(Map.of("text", systemPrompt))));
		}
		body.put("contents", contents);
		body.put("generationConfig", Map.of("temperature", 0.3, "maxOutputTokens", 600));

		Map<String, Object> response = restClient.post().uri("/models/{model}:generateContent", model)
				.contentType(MediaType.APPLICATION_JSON).body(body).retrieve().body(JSON_MAP);

		return extractText(response);
	}

	/**
	 * 받는 모양에서 글자만 꺼내기: {"candidates":[{"content":{"parts":[{"text":"답"}]}}], ...}
	 * 답이 비어 있으면(안전 필터 등) 빈 글자. 생각 과정(thought=true) 조각은 빼고 답만
	 */
	private static String extractText(Map<String, Object> response) {
		if (response != null && response.get("candidates") instanceof List<?> candidates && !candidates.isEmpty()
				&& candidates.get(0) instanceof Map<?, ?> first && first.get("content") instanceof Map<?, ?> content
				&& content.get("parts") instanceof List<?> parts) {
			StringBuilder text = new StringBuilder();
			for (Object part : parts) {
				if (part instanceof Map<?, ?> p && p.get("text") instanceof String t
						&& !Boolean.TRUE.equals(p.get("thought"))) {
					text.append(t);
				}
			}
			return text.toString().strip();
		}
		return "";
	}
}