package com.englandcamping.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** 공지사항 한 개 */
@Entity
@Table(name = "notice")
public class Notice {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String title;

	/** 본문 (길 수 있어서 TEXT, 줄바꿈 그대로 저장) */
	@Column(nullable = false, columnDefinition = "TEXT")
	private String content;

	/** 상단 고정 여부 (게시 기간이 끝나면 화면에서는 고정이 풀린 것으로 봄 → isPinnedNow) */
	@Column(nullable = false)
	private boolean pinned;

	/**
	 * 분류 (DB에는 "NOTICE" 같은 글자로 저장). 칸을 새로 만들 때 예전 공지는 비어 있으므로 null 허용 →
	 * getCategory()에서 "안내"로 봄
	 */
	@Enumerated(EnumType.STRING)
	@Column(length = 20)
	private NoticeCategory category;

	/** 게시 기간 시작일 (없어도 됨) */
	private LocalDate startDate;

	/** 게시 기간 끝나는 날 (없어도 됨, 이 날까지 포함) */
	private LocalDate endDate;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	/** 마지막 수정 시간 (한 번도 안 고쳤으면 null) */
	private LocalDateTime updatedAt;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected Notice() {
	}

	public Notice(String title, String content, boolean pinned, NoticeCategory category, LocalDate startDate,
			LocalDate endDate) {
		this.title = title;
		this.content = content;
		this.pinned = pinned;
		this.category = category;
		this.startDate = startDate;
		this.endDate = endDate;
		this.createdAt = LocalDateTime.now();
	}

	/** 수정 */
	public void update(String title, String content, boolean pinned, NoticeCategory category, LocalDate startDate,
			LocalDate endDate) {
		this.title = title;
		this.content = content;
		this.pinned = pinned;
		this.category = category;
		this.startDate = startDate;
		this.endDate = endDate;
		this.updatedAt = LocalDateTime.now();
	}

	/** 게시 기간이 끝났는지 (끝나는 날 다음 날부터 true, 끝나는 날이 없으면 늘 false) */
	public boolean isEnded() {
		return endDate != null && endDate.isBefore(LocalDate.now());
	}

	/** 지금 고정으로 보여 줄지 (고정이어도 기간이 끝났으면 false) — 화면의 "고정" 배지·주황 세로선 */
	public boolean isPinnedNow() {
		return pinned && !isEnded();
	}

	/** 게시 기간 글자 모양 (예: 2026.11.27) — static이라 DB 칸이 아님 */
	private static final DateTimeFormatter PERIOD_FORMAT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

	/**
	 * 게시 기간 글자 (화면에 그대로 씀). 둘 다 있으면 "2026.11.27 ~ 2026.11.28", 시작만 "2026.11.27부터",
	 * 끝만 "2026.11.28까지", 둘 다 없으면 null (→ 화면에서 기간 줄을 안 보임)
	 */
	public String getPeriodText() {
		if (startDate == null && endDate == null) {
			return null;
		}
		if (endDate == null) {
			return startDate.format(PERIOD_FORMAT) + "부터";
		}
		if (startDate == null) {
			return endDate.format(PERIOD_FORMAT) + "까지";
		}
		return startDate.format(PERIOD_FORMAT) + " ~ " + endDate.format(PERIOD_FORMAT);
	}

	public Long getId() {
		return id;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public boolean isPinned() {
		return pinned;
	}

	/** 분류 (예전 공지처럼 비어 있으면 "안내") */
	public NoticeCategory getCategory() {
		return category == null ? NoticeCategory.NOTICE : category;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}