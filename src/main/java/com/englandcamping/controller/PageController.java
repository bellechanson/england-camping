package com.englandcamping.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.englandcamping.domain.CampSite;
import com.englandcamping.service.SiteGuideService;

/**
 * 메뉴의 하위 페이지들 (소개 / 사이트·요금 / 이용안내 / 오시는 길) 글/링크(settings), 예약 버튼(reserveLinks)
 * 등은 GlobalModelAdvice가 자동으로 넣어 준다.
 */
@Controller
public class PageController {

	private final SiteGuideService siteGuideService;
	/** 카카오맵 JavaScript 키 (application.properties → 환경 변수) */
	private final String kakaoMapJsKey;

	public PageController(SiteGuideService siteGuideService, @Value("${kakao.map.js-key:}") String kakaoMapJsKey) {
		this.siteGuideService = siteGuideService;
		this.kakaoMapJsKey = kakaoMapJsKey;
	}

	/** 소개 */
	@GetMapping("/about")
	public String about() {
		return "pages/about";
	}

	/** 사이트 · 요금 */
	@GetMapping("/sites")
	public String sites(Model model) {
		List<CampSite> sites = siteGuideService.findAllSites();
		model.addAttribute("types", siteGuideService.findAllTypes());
		model.addAttribute("sites", sites);
		// 요금표 아래 "사이트 이용 안내" (지금 불가 + 불가 예정)
		model.addAttribute("siteNotices", siteGuideService.findStatusNotices(sites));
		return "pages/sites";
	}

	/** 이용안내 (추가 인원 요금을 보여 주려고 대표 종류도 넘김) */
	@GetMapping("/guide")
	public String guide(Model model) {
		model.addAttribute("mainType", siteGuideService.findMainType());
		return "pages/guide";
	}

	/** 오시는 길 (카카오맵) */
	@GetMapping("/location")
	public String location(Model model) {
		model.addAttribute("kakaoMapJsKey", kakaoMapJsKey);
		return "pages/location";
	}
}