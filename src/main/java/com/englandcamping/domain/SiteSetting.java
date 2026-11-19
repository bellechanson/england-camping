package com.englandcamping.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 홈페이지 글/링크 한 줄 = 이 테이블의 한 행. 예) setting_key = "PHONE", setting_value =
 * "010-0000-0000" → 항목이 늘어나도 테이블 구조를 바꿀 필요가 없다.
 */
@Entity
@Table(name = "site_setting")
public class SiteSetting {

	/** SettingKey 이름 (예: "INTRO_TITLE") — 이게 기본키 */
	@Id
	@Column(name = "setting_key", length = 50)
	private String settingKey;

	/** 실제 내용. 소개 본문처럼 길 수 있어서 TEXT */
	@Column(name = "setting_value", columnDefinition = "TEXT")
	private String settingValue;

	/** 마지막으로 바뀐 시간 (관리자 화면에서 "언제 고쳤는지" 보여 줄 용도) */
	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected SiteSetting() {
	}

	public SiteSetting(String settingKey, String settingValue) {
		this.settingKey = settingKey;
		this.settingValue = settingValue;
		this.updatedAt = LocalDateTime.now();
	}

	/** 값 바꾸기 (5단계 관리자 화면에서 사용) */
	public void changeValue(String settingValue) {
		this.settingValue = settingValue;
		this.updatedAt = LocalDateTime.now();
	}

	public String getSettingKey() {
		return settingKey;
	}

	public String getSettingValue() {
		return settingValue;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}
}