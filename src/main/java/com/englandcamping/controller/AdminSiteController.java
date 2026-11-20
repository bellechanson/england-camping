package com.englandcamping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.dto.CampSiteForm;
import com.englandcamping.dto.SiteTypeForm;
import com.englandcamping.service.SiteGuideService;

import jakarta.validation.Valid;

/**
 * 관리자: 요금(사이트 종류) 수정 + 싸이트 이용 가능/불가·메모 - /admin/prices 요금 목록 -
 * /admin/prices/{id}/edit 요금 수정 화면 - /admin/sites 싸이트 22개 한 화면에서 저장
 */
@Controller
@RequestMapping("/admin")
public class AdminSiteController {

	private final SiteGuideService siteGuideService;

	public AdminSiteController(SiteGuideService siteGuideService) {
		this.siteGuideService = siteGuideService;
	}

	// ===== 요금 =====

	/** 요금 목록 (사이트 종류마다 한 줄, 지금은 하나) */
	@GetMapping("/prices")
	public String prices(Model model) {
		model.addAttribute("types", siteGuideService.findAllTypes());
		return "admin/prices";
	}

	/** 요금 수정 화면 */
	@GetMapping("/prices/{id}/edit")
	public String priceForm(@PathVariable Long id, Model model) {
		model.addAttribute("form", SiteTypeForm.from(siteGuideService.getType(id)));
		model.addAttribute("typeId", id);
		return "admin/price-form";
	}

	/** 요금 저장 */
	@PostMapping("/prices/{id}")
	public String updatePrice(@PathVariable Long id, @Valid @ModelAttribute("form") SiteTypeForm form,
			BindingResult bindingResult, Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("typeId", id);
			return "admin/price-form"; // 입력한 내용 그대로 + 오류 문구
		}
		siteGuideService.updateType(id, form);
		redirect.addFlashAttribute("message", "'" + form.getName().strip() + "' 요금을 저장했습니다.");
		return "redirect:/admin/prices";
	}

	// ===== 싸이트 =====

	/** 싸이트 관리 화면 */
	@GetMapping("/sites")
	public String sites(Model model) {
		model.addAttribute("form", CampSiteForm.from(siteGuideService.findAllSites()));
		return "admin/sites";
	}

	/** 싸이트 전체 저장 (바뀐 줄만 실제로 고침) */
	@PostMapping("/sites")
	public String updateSites(@Valid @ModelAttribute("form") CampSiteForm form, BindingResult bindingResult,
			Model model, RedirectAttributes redirect) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("error", "메모는 200자 이내로 입력해 주세요. 빨간 글씨가 있는 줄을 확인해 주세요.");
			return "admin/sites";
		}
		int changed = siteGuideService.updateSiteStatuses(form.getRows());
		redirect.addFlashAttribute("message", changed == 0 ? "바뀐 내용이 없습니다." : "싸이트 " + changed + "개를 저장했습니다.");
		return "redirect:/admin/sites";
	}
}