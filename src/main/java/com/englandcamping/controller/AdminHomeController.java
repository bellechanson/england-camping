package com.englandcamping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.englandcamping.ai.ChatRateLimiter;
import com.englandcamping.service.NoticeService;
import com.englandcamping.service.PhotoService;
import com.englandcamping.service.SiteGuideService;

/**
 * 관리자 첫 화면 (/admin) — "무엇을 고칠까요?" 메뉴 카드 + 간단한 현황 숫자 로그인하면 여기로 옴
 * (SecurityConfig의 defaultSuccessUrl("/admin"))
 */
@Controller
public class AdminHomeController {

	private final NoticeService noticeService;
	private final PhotoService photoService;
	private final SiteGuideService siteGuideService;
	private final ChatRateLimiter chatLimiter;

	public AdminHomeController(NoticeService noticeService, PhotoService photoService,
			SiteGuideService siteGuideService, ChatRateLimiter chatLimiter) {
		this.noticeService = noticeService;
		this.photoService = photoService;
		this.siteGuideService = siteGuideService;
		this.chatLimiter = chatLimiter;
	}

	@GetMapping("/admin")
	public String home(Model model) {
		model.addAttribute("noticeCount", noticeService.countAll());
		model.addAttribute("photoCount", photoService.countAll());
		model.addAttribute("siteCount", siteGuideService.countAllSites());
		model.addAttribute("closedSiteCount", siteGuideService.countClosedSites());
		// 요금 카드에 평소 1박 요금을 보여 주려고 (없으면 null)
		model.addAttribute("mainType", siteGuideService.findMainType());
		// AI 챗봇 카드: 오늘 질문 수 (깨어 있는지는 settings['CHATBOT_ENABLED'])
		model.addAttribute("chatToday", chatLimiter.getTodayTotal());
		model.addAttribute("chatLimit", chatLimiter.getDailyTotalLimit());
		return "admin/home";
	}
}