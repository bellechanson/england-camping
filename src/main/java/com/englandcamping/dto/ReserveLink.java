package com.englandcamping.dto;

/** 예약 버튼 하나 (화면에 보여 줄 이름 + 이동할 주소) */
public class ReserveLink {

	private final String name;
	private final String url;

	public ReserveLink(String name, String url) {
		this.name = name;
		this.url = url;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}
}