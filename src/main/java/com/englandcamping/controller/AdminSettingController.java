package com.englandcamping.controller;

import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.domain.SettingGroup;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.service.SiteSettingService;

/**
 * 관리자: 글/링크 설정 (한 화면 + 묶음 탭) - 탭은 주소로 바꿈: /admin/settings?tab=CONTACT (자바스크립트
 * 없이 동작) - 저장은 탭(묶음)마다 따로: POST /admin/settings/CONTACT
 */
@Controller
@RequestMapping("/admin/settings")
public class AdminSettingController {

	private final SiteSettingService siteSettingService;

	public AdminSettingController(SiteSettingService siteSettingService) {
		this.siteSettingService = siteSettingService;
	}

	/** 설정 화면 (tab 없으면 첫 탭 "소개") */
	@GetMapping
	public String form(@RequestParam(defaultValue = "INTRO") String tab, Model model) {
		return show(SettingGroup.from(tab), siteSettingService.getAllAsMap(), model);
	}

	/** 탭 하나 저장 */
	@PostMapping("/{tab}")
	public String save(@PathVariable String tab, @RequestParam Map<String, String> params, Model model,
			RedirectAttributes redirect) {
		SettingGroup group = SettingGroup.from(tab);
		try {
			siteSettingService.updateGroup(group, params);
		} catch (IllegalArgumentException e) {
			// 저장 안 됨 → 입력한 내용을 그대로 다시 보여 주고 이유 안내
			Map<String, String> values = siteSettingService.getAllAsMap();
			for (SettingKey key : group.getKeys()) {
				if (params.containsKey(key.name())) {
					values.put(key.name(), params.get(key.name()));
				}
			}
			model.addAttribute("error", e.getMessage());
			return show(group, values, model);
		}
		redirect.addFlashAttribute("message", "'" + group.getLabel() + "' 내용을 저장했습니다. 홈페이지에 바로 반영됩니다.");
		return "redirect:/admin/settings?tab=" + group.name();
	}

	/** 화면에 필요한 값 넣고 템플릿으로 */
	private String show(SettingGroup group, Map<String, String> values, Model model) {
		model.addAttribute("groups", SettingGroup.values()); // 탭 목록
		model.addAttribute("group", group); // 지금 탭
		model.addAttribute("values", values); // 입력 칸 값 (settings와 따로: 오류 때 입력값 유지용)
		return "admin/settings";
	}
}