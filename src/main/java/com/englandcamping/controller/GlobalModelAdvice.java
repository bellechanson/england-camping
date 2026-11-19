package com.englandcamping.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.englandcamping.domain.SettingKey;
import com.englandcamping.dto.ReserveLink;
import com.englandcamping.service.SiteSettingService;

/**
 * 모든 페이지에 공통으로 넣어 주는 값. 머리글(메뉴)과 꼬리말(주소, 전화)은 페이지마다 다 필요해서 컨트롤러마다 따로 넣지 않고 여기서
 * 한 번에 넣는다.
 */
@ControllerAdvice
public class GlobalModelAdvice {

	private final SiteSettingService siteSettingService;

	public GlobalModelAdvice(SiteSettingService siteSettingService) {
		this.siteSettingService = siteSettingService;
	}

	/** 글/링크 전체 → 화면에서 ${settings['CAMP_NAME']} */
	@ModelAttribute("settings")
	public Map<String, String> settings() {
		return siteSettingService.getAllAsMap();
	}

	/** 예약 버튼 목록 (주소가 비어 있는 예약처는 버튼을 만들지 않음) */
	@ModelAttribute("reserveLinks")
	public List<ReserveLink> reserveLinks() {
		List<ReserveLink> links = new ArrayList<>();
		addIfPresent(links, "네이버 예약", SettingKey.RESERVE_NAVER_URL);
		addIfPresent(links, "캠핏", SettingKey.RESERVE_CAMFIT_URL);
		addIfPresent(links, "땡큐캠핑", SettingKey.RESERVE_THANKYOU_URL);
		return links;
	}

	/** 소개 본문을 문단 목록으로 */
	@ModelAttribute("introParagraphs")
	public List<String> introParagraphs() {
		return siteSettingService.getLines(SettingKey.INTRO_BODY);
	}

	/** 이용 규칙을 한 줄씩 목록으로 */
	@ModelAttribute("ruleLines")
	public List<String> ruleLines() {
		return siteSettingService.getLines(SettingKey.RULES);
	}

	/**
	 * 띠 공지: 켜져 있고 내용이 있을 때만 그 글자, 아니면 null → 머리글에서 null이면 띠를 아예 안 그림 (모든 손님 페이지 공통)
	 */
	@ModelAttribute("banner")
	public String banner() {
		boolean enabled = "true".equals(siteSettingService.get(SettingKey.BANNER_ENABLED));
		String text = siteSettingService.get(SettingKey.BANNER_TEXT);
		return (enabled && text != null && !text.isBlank()) ? text.strip() : null;
	}

	private void addIfPresent(List<ReserveLink> links, String name, SettingKey key) {
		String url = siteSettingService.get(key);
		if (url != null && !url.isBlank()) {
			links.add(new ReserveLink(name, url.strip()));
		}
	}
}