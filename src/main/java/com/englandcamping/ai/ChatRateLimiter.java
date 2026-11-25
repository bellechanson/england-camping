package com.englandcamping.ai;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 챗봇 "출입 기록부" — ① 전체 하루 질문 수(구글 무료 한도) ② 한 사람(접속 주소)당 하루 질문 수 - "하루" = 구글 기준
 * (미국 서부 자정 = 한국 오후 5시 겨울 / 오후 4시 여름) → 구글 한도와 같은 때 0으로 - 메모리에만 기록 (DB 안 씀).
 * 서버를 재시작하면 0부터 → 그사이 구글 쪽이 다 찼으면 429 를 받고 markTotalExhausted() - 접속 주소(IP)만
 * 세고, 질문 내용은 기록하지 않음
 */
@Component
public class ChatRateLimiter {

	/** tryUse 결과: 전체 하루 한도가 다 참 */
	public static final int TOTAL_EXCEEDED = -2;
	/** tryUse 결과: 이 사람의 하루 한도가 다 참 */
	public static final int USER_EXCEEDED = -1;

	/** 구글 무료 한도가 초기화되는 기준 시간대 */
	private static final ZoneId GOOGLE_ZONE = ZoneId.of("America/Los_Angeles");

	/** 기록부가 너무 커지는 것 방지 (장난으로 주소를 바꿔 가며 보내는 경우) */
	private static final int MAX_ENTRIES = 50_000;

	private final int dailyTotalLimit;
	private final int dailyLimitPerUser;
	private final Map<String, AtomicInteger> userCounts = new ConcurrentHashMap<>();
	private final AtomicInteger total = new AtomicInteger();
	private LocalDate day = LocalDate.now(GOOGLE_ZONE);

	public ChatRateLimiter(@Value("${chatbot.daily-total-limit:500}") int dailyTotalLimit,
			@Value("${chatbot.daily-limit-per-user:20}") int dailyLimitPerUser) {
		this.dailyTotalLimit = dailyTotalLimit;
		this.dailyLimitPerUser = dailyLimitPerUser;
	}

	/**
	 * 질문 한 번 쓰기 (구글에 묻기 전에 부름)
	 * 
	 * @return 이 사람의 오늘 남은 횟수(0 이상) / USER_EXCEEDED / TOTAL_EXCEEDED
	 */
	public synchronized int tryUse(String userKey) {
		resetIfNewDay();

		// ① 전체 한도 먼저 (다 찼으면 개인 횟수는 깎지 않음)
		if (total.get() >= dailyTotalLimit) {
			return TOTAL_EXCEEDED;
		}

		// ② 개인 한도
		if (userCounts.size() > MAX_ENTRIES) {
			userCounts.clear();
		}
		AtomicInteger count = userCounts.computeIfAbsent(userKey, k -> new AtomicInteger());
		if (count.get() >= dailyLimitPerUser) {
			return USER_EXCEEDED;
		}

		count.incrementAndGet();
		total.incrementAndGet();
		return dailyLimitPerUser - count.get();
	}

	/** 구글이 "오늘치 다 썼어(429)"라고 하면 → 우리 쪽도 그날은 종료 상태로 */
	public synchronized void markTotalExhausted() {
		resetIfNewDay();
		total.set(dailyTotalLimit);
	}

	/** 오늘(구글 기준) 챗봇이 쓴 질문 수 — 관리자 화면용 (6단계) */
	public synchronized int getTodayTotal() {
		resetIfNewDay();
		return total.get();
	}

	/** 다음에 0으로 돌아가는 시각 (한국 시간) — 관리자 화면 안내용. 겨울 오후 5시 / 여름 오후 4시 */
	public ZonedDateTime getNextResetKst() {
		return LocalDate.now(GOOGLE_ZONE).plusDays(1).atStartOfDay(GOOGLE_ZONE)
				.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
	}

	public int getDailyTotalLimit() {
		return dailyTotalLimit;
	}

	public int getDailyLimitPerUser() {
		return dailyLimitPerUser;
	}

	/** 구글 기준 날짜가 바뀌었으면 기록부 비우기 */
	private void resetIfNewDay() {
		LocalDate today = LocalDate.now(GOOGLE_ZONE);
		if (!today.equals(day)) {
			userCounts.clear();
			total.set(0);
			day = today;
		}
	}
}