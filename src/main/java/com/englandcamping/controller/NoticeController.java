package com.englandcamping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.englandcamping.service.NoticeService;

/** 손님용 공지사항 (목록 / 상세) — 로그인 없이 누구나 */
@Controller
@RequestMapping("/notice")
public class NoticeController {

	private final NoticeService noticeService;

	public NoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	/** 목록 (?page=0 부터) */
	@GetMapping
	public String list(@RequestParam(defaultValue = "0") int page, Model model) {
		model.addAttribute("page", noticeService.findPage(page));
		// 이 시각 이후에 쓴 글이면 NEW 배지 (기간은 NoticeService.NEW_DAYS)
		model.addAttribute("newSince", noticeService.newSince());
		return "pages/notice-list";
	}

	/** 상세 + 이전/다음 글 + 오른쪽 최근 소식 3개 (없는 번호면 404) */
	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("notice", noticeService.get(id));
		model.addAttribute("prevNotice", noticeService.findPrevious(id));
		model.addAttribute("nextNotice", noticeService.findNext(id));
		// 오른쪽 "최근 소식" (메인과 같은 3개, 고정 공지 먼저)
		model.addAttribute("recentNotices", noticeService.findRecent());
		return "pages/notice-detail";
	}
}