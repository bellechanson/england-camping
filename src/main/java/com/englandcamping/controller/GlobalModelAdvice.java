package com.englandcamping.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.englandcamping.domain.Photo;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.dto.ReserveLink;
import com.englandcamping.service.PhotoService;
import com.englandcamping.service.SiteSettingService;

/**
 * 모든 페이지에 공통으로 넣어 주는 값. 머리글(메뉴)과 꼬리말(주소, 전화)은 페이지마다 다 필요해서 컨트롤러마다 따로 넣지 않고 여기서
 * 한 번에 넣는다.
 *
 * basePackages: 우리 컨트롤러에만 적용. 스프링의 오류 화면(/error)에는 넣지 않는다 → DB가 멈춰서 오류가 났을 때 오류
 * 화면까지 DB를 읽다가 같이 깨지는 것을 막음 (error.html은 이 값 없이 그려짐)
 */
@ControllerAdvice(basePackages = "com.englandcamping.controller")
public class GlobalModelAdvice {

	private final SiteSettingService siteSettingService;
	private final PhotoService photoService;

	public GlobalModelAdvice(SiteSettingService siteSettingService, PhotoService photoService) {
		this.siteSettingService = siteSettingService;
		this.photoService = photoService;
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

	/**
	 * 사진 자리 → 화면에서 ${slotPhotos['HERO']} (지정 안 된 자리는 null → 그라데이션) 메인, 소개, 사이트·요금,
	 * 관리자 사진 관리, 카톡 미리보기 사진에서 사용
	 */
	@ModelAttribute("slotPhotos")
	public Map<String, Photo> slotPhotos() {
		return photoService.findSlotPhotos();
	}

	/**
	 * 사이트 기본 주소 (예: http://localhost:8044) 카톡·검색 미리보기의 사진 주소는 "http://..."부터 전부 적어야
	 * 해서 필요 (배포 때 nginx 뒤에 두면 server.forward-headers-strategy 설정 필요 → 6단계)
	 */
	@ModelAttribute("baseUrl")
	public String baseUrl() {
		return ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();
	}

	/** 지금 페이지의 전체 주소 (? 뒤는 뺌) — 미리보기의 og:url, 검색용 canonical */
	@ModelAttribute("currentUrl")
	public String currentUrl() {
		return ServletUriComponentsBuilder.fromCurrentRequestUri().replaceQuery(null).build().toUriString();
	}

	/**
	 * 관리자로 로그인한 상태인지 → 손님 화면 왼쪽 아래 "관리" 바로가기를 보여 줄지 (로그인 안 한 손님은 "anonymous" 로그인으로
	 * 잡히므로 그건 빼고 판단)
	 */
	@ModelAttribute("adminLoggedIn")
	public boolean adminLoggedIn() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken);
	}

	private void addIfPresent(List<ReserveLink> links, String name, SettingKey key) {
		String url = siteSettingService.get(key);
		if (url != null && !url.isBlank()) {
			links.add(new ReserveLink(name, url.strip()));
		}
	}
}