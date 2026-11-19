package com.englandcamping.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.englandcamping.domain.CampSite;
import com.englandcamping.domain.SiteType;
import com.englandcamping.service.NoticeService;
import com.englandcamping.service.SiteGuideService;

/** 메인 페이지 */
@Controller
public class HomeController {

	private final SiteGuideService siteGuideService;
	private final NoticeService noticeService;

	public HomeController(SiteGuideService siteGuideService, NoticeService noticeService) {
		this.siteGuideService = siteGuideService;
		this.noticeService = noticeService;
	}

	@GetMapping("/")
	public String home(Model model) {
		List<SiteType> types = siteGuideService.findAllTypes();
		List<CampSite> sites = siteGuideService.findAllSites();

		// 지금은 사이트 종류가 하나뿐이라 첫 번째를 "대표 종류"로 사용
		// (데이터가 비어 있으면 null → 화면에서 요금 부분을 숨김)
		model.addAttribute("mainType", types.isEmpty() ? null : types.get(0));
		model.addAttribute("sites", sites);
		model.addAttribute("siteCount", sites.size());

		// 최근 공지 3개 (고정 공지 먼저). 없으면 메인에서 공지 칸을 숨김
		model.addAttribute("recentNotices", noticeService.findRecent());

		return "index"; // templates/index.html
	}
}