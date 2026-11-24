package com.englandcamping.controller;

import java.security.Principal;
import java.time.LocalDate;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.domain.SettingKey;
import com.englandcamping.dto.PasswordForm;
import com.englandcamping.service.AdminAccountService;
import com.englandcamping.service.SiteSettingService;

/**
 * 관리자 공통: 로그인 화면, 띠 공지 저장, 비밀번호 변경 - 로그인 "처리"는 Spring Security가 하고, 여기서는 화면만 보여
 * 줌 - 관리자 첫 화면(/admin)은 AdminHomeController - 비밀번호 변경은 머리글의
 * 팝업(fragments/admin)에서 보냄
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

	/** 띠 공지 최대 글자 수 */
	private static final int BANNER_MAX_LENGTH = 100;

	/** 띠 공지는 공지 관리 화면 안에 있어서, 저장 후 그 화면으로 */
	private static final String REDIRECT_NOTICES = "redirect:/admin/notices";

	/** 관리자 첫 화면 */
	private static final String REDIRECT_HOME = "redirect:/admin";

	private final AdminAccountService adminAccountService;
	private final SiteSettingService siteSettingService;

	public AdminController(AdminAccountService adminAccountService, SiteSettingService siteSettingService) {
		this.adminAccountService = adminAccountService;
		this.siteSettingService = siteSettingService;
	}

	/** 로그인 화면 */
	@GetMapping("/login")
	public String login() {
		return "admin/login";
	}

	/** 띠 공지 저장 (켜기/끄기 + 내용 + 끝나는 날(선택)) */
	@PostMapping("/banner")
	public String updateBanner(@RequestParam(defaultValue = "false") boolean enabled,
			@RequestParam(defaultValue = "") String text,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
			RedirectAttributes redirect) {
		String trimmed = text.strip();

		if (enabled && trimmed.isEmpty()) {
			redirect.addFlashAttribute("error", "띠 공지를 켜려면 내용을 입력해 주세요.");
			return REDIRECT_NOTICES;
		}
		if (trimmed.length() > BANNER_MAX_LENGTH) {
			redirect.addFlashAttribute("error", "띠 공지는 " + BANNER_MAX_LENGTH + "자 이내로 입력해 주세요.");
			return REDIRECT_NOTICES;
		}
		if (enabled && endDate != null && endDate.isBefore(LocalDate.now())) {
			redirect.addFlashAttribute("error", "끝나는 날이 이미 지났습니다. 오늘이나 그 뒤로 골라 주세요.");
			return REDIRECT_NOTICES;
		}

		siteSettingService.update(SettingKey.BANNER_TEXT, trimmed);
		siteSettingService.update(SettingKey.BANNER_ENABLED, String.valueOf(enabled));
		// 날짜 칸을 비우면 "" → 기간 없음
		siteSettingService.update(SettingKey.BANNER_END_DATE, endDate == null ? "" : endDate.toString());
		redirect.addFlashAttribute("message", enabled ? "띠 공지를 켰습니다." : "띠 공지를 저장했습니다. (꺼짐)");
		return REDIRECT_NOTICES;
	}

	/** 예전 비밀번호 변경 주소로 들어오면 관리자 첫 화면으로 (이제는 팝업) */
	@GetMapping("/password")
	public String passwordPage() {
		return REDIRECT_HOME;
	}

	/**
	 * 비밀번호 변경 처리 (머리글 팝업에서 보냄). 성공·실패 모두 원래 보던 화면(returnTo)으로 돌아감. 실패하면
	 * passwordError → 그 화면에서 팝업이 다시 열리고 이유 표시 (입력한 비밀번호는 다시 채우지 않음)
	 */
	@PostMapping("/password")
	public String changePassword(@ModelAttribute PasswordForm form,
			@RequestParam(defaultValue = "/admin") String returnTo, Principal principal, RedirectAttributes redirect) {
		String back = "redirect:" + safeAdminPath(returnTo);

		if (!Objects.equals(form.getNewPassword(), form.getNewPasswordConfirm())) {
			redirect.addFlashAttribute("passwordError", "새 비밀번호 두 칸이 서로 다릅니다.");
			return back;
		}
		try {
			adminAccountService.changePassword(principal.getName(), form.getCurrentPassword(), form.getNewPassword());
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("passwordError", e.getMessage());
			return back;
		}
		redirect.addFlashAttribute("message", "비밀번호를 바꿨습니다.");
		return back;
	}

	/**
	 * 돌아갈 주소 검사: 관리자 화면 주소(/admin, /admin/notices, /admin/prices/1/edit ...)만 허용. 그
	 * 외(다른 사이트 주소 등)는 관리자 첫 화면으로 → 다른 사이트로 튕겨 보내기 막음
	 */
	private static String safeAdminPath(String path) {
		if (path != null && path.matches("/admin(/[\\w\\-/]*)?")) {
			return path;
		}
		return "/admin";
	}
}