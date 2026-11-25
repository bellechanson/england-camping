package com.englandcamping.controller;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.ai.ChatRateLimiter;
import com.englandcamping.ai.GeminiClient;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.service.SiteSettingService;

/**
 * 관리자: AI 챗봇(영국이) — 깨우기/재우기, 추가 안내 글, 오늘 질문 수. 깨어 있는지와 추가 안내 글은
 * ${settings[...]} 로 화면에 들어감 (GlobalModelAdvice)
 */
@Controller
@RequestMapping("/admin/chatbot")
public class AdminChatbotController {

	/** "오후 5시" 모양 */
	private static final DateTimeFormatter RESET_FORMAT = DateTimeFormatter.ofPattern("a h시", Locale.KOREAN);

	private final SiteSettingService siteSettingService;
	private final ChatRateLimiter limiter;
	private final GeminiClient gemini;

	public AdminChatbotController(SiteSettingService siteSettingService, ChatRateLimiter limiter, GeminiClient gemini) {
		this.siteSettingService = siteSettingService;
		this.limiter = limiter;
		this.gemini = gemini;
	}

	/** AI 챗봇 화면 */
	@GetMapping
	public String page(Model model) {
		int used = limiter.getTodayTotal();
		int limit = limiter.getDailyTotalLimit();
		model.addAttribute("hasKey", gemini.hasKey());
		model.addAttribute("todayUsed", used);
		model.addAttribute("todayLimit", limit);
		model.addAttribute("todayLeft", Math.max(0, limit - used));
		model.addAttribute("usedPercent", limit <= 0 ? 100 : Math.min(100, used * 100 / limit));
		model.addAttribute("perUserLimit", limiter.getDailyLimitPerUser());
		model.addAttribute("resetTime", limiter.getNextResetKst().format(RESET_FORMAT));
		model.addAttribute("extraMax", SettingKey.CHATBOT_EXTRA_INFO.getMaxLength());
		return "admin/chatbot";
	}

	/**
	 * 저장 (깨우기/재우기 + 추가 안내). 추가 안내 정리·글자 수 검사는 글/링크 설정과 같은 규칙 (SiteSettingService)
	 */
	@PostMapping
	public String save(@RequestParam(defaultValue = "false") boolean enabled,
			@RequestParam(defaultValue = "") String extraInfo, RedirectAttributes redirect) {
		try {
			siteSettingService.updateOne(SettingKey.CHATBOT_EXTRA_INFO, extraInfo);
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
			return "redirect:/admin/chatbot";
		}
		siteSettingService.update(SettingKey.CHATBOT_ENABLED, String.valueOf(enabled));
		redirect.addFlashAttribute("message",
				enabled ? "저장했습니다. 영국이가 깨어 있어요." : "저장했습니다. 영국이를 재웠어요. (홈페이지에서 자는 모습으로 보여요)");
		return "redirect:/admin/chatbot";
	}
}