package com.englandcamping.domain;

/** 공지 분류 (말머리) — 목록·상세에 작은 배지로 보임 */
public enum NoticeCategory {

	NOTICE("안내"), CONSTRUCTION("공사"), CLOSED("휴장"), EVENT("이벤트");

	/** 화면에 보여 줄 이름 */
	private final String label;

	NoticeCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}