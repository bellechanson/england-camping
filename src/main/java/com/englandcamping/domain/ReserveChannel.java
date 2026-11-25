package com.englandcamping.domain;

/**
 * 사이트를 어느 예약처에서 받는지 (사이트마다 하나). - 네이버와 캠핏은 서로 연동돼 있어서 한 묶음 - 땡큐캠핑은 예약 프로그램 회사가
 * 달라 공유가 안 됨 → 사이트를 따로 나눠서 받음 (중복 예약 방지) - 어느 쪽도 아니면 전화 문의 DB에는 이름(NAVER_CAMFIT
 * 등) 글자로 저장됨 → 순서를 바꿔도 안전
 */
public enum ReserveChannel {

	NAVER_CAMFIT("네이버·캠핏"), THANKQ("땡큐캠핑"), PHONE("전화 문의");

	/** 화면에 보여 줄 이름 */
	private final String label;

	ReserveChannel(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}