package com.englandcamping.dto;

/** 예약 버튼 하나 (화면에 보여 줄 이름 + 이동할 주소 + 로고 그림 주소) */
public class ReserveLink {

	private final String name;
	private final String url;
	/** 로고 그림 주소 (예: /assets/img/naverLogo.png). 없으면 null → 화면에서 글자만 */
	private final String icon;

	public ReserveLink(String name, String url, String icon) {
		this.name = name;
		this.url = url;
		this.icon = icon;
	}

	public String getName() {
		return name;
	}

	public String getUrl() {
		return url;
	}

	public String getIcon() {
		return icon;
	}
}