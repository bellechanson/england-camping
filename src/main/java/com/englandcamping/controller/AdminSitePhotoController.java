package com.englandcamping.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.englandcamping.domain.CampSite;
import com.englandcamping.dto.CampSiteForm;
import com.englandcamping.service.SitePhotoService;

/**
 * 관리자: 사이트 사진 (사이트마다 1장, 손님 배치도에서 동그라미를 누르면 보임) - GET /admin/site-photos 화면 -
 * POST /admin/site-photos/{id} 올리기/바꾸기 - POST /admin/site-photos/{id}/delete
 * 지우기
 */
@Controller
@RequestMapping("/admin/site-photos")
public class AdminSitePhotoController {

	private static final String REDIRECT = "redirect:/admin/site-photos";

	private final SitePhotoService sitePhotoService;

	public AdminSitePhotoController(SitePhotoService sitePhotoService) {
		this.sitePhotoService = sitePhotoService;
	}

	/** 사이트 사진 화면 (위치 묶음은 사이트 관리 화면과 같은 규칙 = CampSiteForm.getGroups) */
	@GetMapping
	public String list(Model model) {
		List<CampSite> sites = sitePhotoService.findAll();
		model.addAttribute("sites", sites);
		model.addAttribute("groups", CampSiteForm.from(sites).getGroups()); // 번호(index)는 sites 의 순서와 같음
		return "admin/site-photos";
	}

	/** 사진 올리기/바꾸기 → 그 사이트 칸으로 돌아옴 */
	@PostMapping("/{id}")
	public String upload(@PathVariable Long id, @RequestParam(value = "file", required = false) MultipartFile file,
			RedirectAttributes redirect) {
		String name = sitePhotoService.get(id).getName();
		try {
			boolean changed = sitePhotoService.upload(id, file);
			redirect.addFlashAttribute("message", "'" + name + "' 사진을 " + (changed ? "바꿨습니다." : "올렸습니다."));
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return REDIRECT + "#site-" + id;
	}

	/** 사진 지우기 (확인 창에서 "지우기"를 눌렀을 때만) */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {
		String name = sitePhotoService.get(id).getName();
		sitePhotoService.deletePhoto(id);
		redirect.addFlashAttribute("message", "'" + name + "' 사진을 지웠습니다.");
		return REDIRECT + "#site-" + id;
	}
}