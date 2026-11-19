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
		return "pages/notice-list";
	}

	/** 상세 + 이전/다음 글 (없는 번호면 404) */
	@GetMapping("/{id}")
	public String detail(@PathVariable Long id, Model model) {
		model.addAttribute("notice", noticeService.get(id));
		model.addAttribute("prevNotice", noticeService.findPrevious(id));
		model.addAttribute("nextNotice", noticeService.findNext(id));
		return "pages/notice-detail";
	}
}