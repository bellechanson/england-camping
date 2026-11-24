package com.englandcamping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.domain.NoticeCategory;
import com.englandcamping.dto.NoticeForm;
import com.englandcamping.service.NoticeService;

import jakarta.validation.Valid;

/** 관리자: 공지 목록 / 쓰기 / 수정 / 삭제 */
@Controller
@RequestMapping("/admin/notices")
public class AdminNoticeController {

	private final NoticeService noticeService;

	public AdminNoticeController(NoticeService noticeService) {
		this.noticeService = noticeService;
	}

	/** 쓰기/수정 화면의 분류 고르기 칸 (안내 / 공사 / 휴장 / 이벤트) — 이 컨트롤러의 모든 화면에 자동으로 들어감 */
	@ModelAttribute("noticeCategories")
	public NoticeCategory[] noticeCategories() {
		return NoticeCategory.values();
	}

	/** 목록 (+ 띠 공지 설정 칸) */
	@GetMapping
	public String list(@RequestParam(defaultValue = "0") int page, Model model) {
		model.addAttribute("page", noticeService.findPage(page));
		return "admin/notices";
	}

	/** 새 공지 쓰기 화면 */
	@GetMapping("/new")
	public String newForm(Model model) {
		model.addAttribute("form", new NoticeForm());
		return "admin/notice-form"; // noticeId 가 없으면 "새 공지"
	}

	/** 새 공지 저장 */
	@PostMapping
	public String create(@Valid @ModelAttribute("form") NoticeForm form, BindingResult bindingResult,
			RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			return "admin/notice-form"; // 입력한 내용 그대로 + 오류 문구
		}
		noticeService.create(form);
		redirect.addFlashAttribute("message", "공지를 등록했습니다.");
		return "redirect:/admin/notices";
	}

	/** 수정 화면 */
	@GetMapping("/{id}/edit")
	public String editForm(@PathVariable Long id, Model model) {
		model.addAttribute("form", NoticeForm.from(noticeService.get(id)));
		model.addAttribute("noticeId", id);
		return "admin/notice-form";
	}

	/** 수정 저장 */
	@PostMapping("/{id}")
	public String update(@PathVariable Long id, @Valid @ModelAttribute("form") NoticeForm form,
			BindingResult bindingResult, Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("noticeId", id);
			return "admin/notice-form";
		}
		noticeService.update(id, form);
		redirect.addFlashAttribute("message", "공지를 수정했습니다.");
		return "redirect:/admin/notices";
	}

	/** 삭제 (화면의 확인 창에서 "삭제"를 눌렀을 때만 들어옴) */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {
		noticeService.delete(id);
		redirect.addFlashAttribute("message", "공지를 삭제했습니다.");
		return "redirect:/admin/notices";
	}
}