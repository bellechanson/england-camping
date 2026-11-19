package com.englandcamping.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

	/** 상단 고정 여부 */
	@Column(nullable = false)
	private boolean pinned;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	/** 마지막 수정 시간 (한 번도 안 고쳤으면 null) */
	private LocalDateTime updatedAt;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected Notice() {
	}

	public Notice(String title, String content, boolean pinned) {
		this.title = title;
		this.content = content;
		this.pinned = pinned;
		this.createdAt = LocalDateTime.now();
	}

	/** 수정 */
	public void update(String title, String content, boolean pinned) {
		this.title = title;
		this.content = content;
		this.pinned = pinned;
		this.updatedAt = LocalDateTime.now();
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}