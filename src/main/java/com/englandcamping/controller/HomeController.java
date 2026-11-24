package com.englandcamping.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.englandcamping.domain.CampSite;
import com.englandcamping.domain.PhotoSlot;
import com.englandcamping.service.NoticeService;
import com.englandcamping.service.PhotoService;
import com.englandcamping.service.SiteGuideService;

/** 메인 페이지 */
@Controller
public class HomeController {

	private final SiteGuideService siteGuideService;
	private final NoticeService noticeService;
	private final PhotoService photoService;

	public HomeController(SiteGuideService siteGuideService, NoticeService noticeService, PhotoService photoService) {
		this.siteGuideService = siteGuideService;
		this.noticeService = noticeService;
		this.photoService = photoService;
	}

	@GetMapping("/")
	public String home(Model model) {
		List<CampSite> sites = siteGuideService.findAllSites();

		// 대표 사이트 종류 (이용안내·관리자 첫 화면과 같은 방법으로 고름)
		// 데이터가 비어 있으면 null → 화면에서 요금 부분을 숨김
		model.addAttribute("mainType", siteGuideService.findMainType());
		model.addAttribute("sites", sites);
		model.addAttribute("siteCount", sites.size());

		// 요금표 아래 "사이트 이용 안내" (지금 불가 + 불가 예정)
		model.addAttribute("siteNotices", siteGuideService.findStatusNotices(sites));

		// 최근 공지 3개 (고정 공지 먼저). 없으면 메인에서 공지 칸을 숨김
		model.addAttribute("recentNotices", noticeService.findRecent());

		// 최근 공지 NEW 배지 기준 (공지 목록과 같음)
		model.addAttribute("newSince", noticeService.newSince());

		// 갤러리 칸: 갤러리 순서 앞에서 5장 (없으면 사진 자리 그대로)
		model.addAttribute("galleryPreview", photoService.findPreview());

		// 메인 큰 사진: 폰에서 보일 부분 (CSS object-position 값, 예: "100% 50%")
		model.addAttribute("heroFocus", photoService.findFocusX(PhotoSlot.HERO) + "% 50%");

		return "index"; // templates/index.html
	}
}