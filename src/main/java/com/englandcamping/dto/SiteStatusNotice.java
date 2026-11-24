package com.englandcamping.dto;

/**
 * 손님 화면 "사이트 이용 안내" 한 줄 (메인·사이트 페이지 요금표 아래). - closedNow = true : 지금 이용 불가
 * (배치도처럼 점선 동그라미) - closedNow = false : 앞으로 불가 예정 (주황 동그라미 + "예정" 딱지) 관리자 메모는
 * 넣지 않음 (손님에게 안 보임)
 */
public class SiteStatusNotice {

	/** 짧은 이름 (07, A ...) */
	private final String shortName;

	/** 지금 이용 불가인지 */
	private final boolean closedNow;

	/** 손님에게 보일 글 (예: "11월 26일까지 이용 불가") */
	private final String text;

	public SiteStatusNotice(String shortName, boolean closedNow, String text) {
		this.shortName = shortName;
		this.closedNow = closedNow;
		this.text = text;
	}

	public String getShortName() {
		return shortName;
	}

	public boolean isClosedNow() {
		return closedNow;
	}

	public String getText() {
		return text;
	}
}