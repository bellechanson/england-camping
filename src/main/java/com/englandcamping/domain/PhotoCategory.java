package com.englandcamping.domain;

/** 갤러리 분류 (갤러리 페이지의 탭) */
public enum PhotoCategory {

	SITE("사이트"), FACILITY("시설"), NATURE("계곡·풍경"), SEASON("사계절");

	/** 화면에 보여 줄 이름 */
	private final String label;

	PhotoCategory(String label) {
		this.label = label;
	}

	public String getLabel() {
		return label;
	}
}