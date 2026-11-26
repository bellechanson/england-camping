package com.englandcamping.domain;

/**
 * 갤러리 분류 (갤러리 페이지의 탭) - 탭 순서 = 아래 적은 순서 (전체 / 시설 / 풍경 / 사계절) - 2026-11
 * "사이트(SITE)" 분류 삭제 (아빠 요청) → 서버 DB에 SITE로 남은 사진이 있으면 갤러리가 오류 나니, 배포 전에 NATURE로
 * 옮겨 둘 것
 */
public enum PhotoCategory {

	FACILITY("시설"), NATURE("풍경"), SEASON("사계절");

	/** 화면에 보여 줄 이름 */
	private final String label;

	PhotoCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}