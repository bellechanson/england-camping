package com.englandcamping.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.englandcamping.domain.PhotoCategory;
import com.englandcamping.service.PhotoService;

/** 손님용 갤러리 (/gallery, 분류 탭은 ?category=SITE 처럼) */
@Controller
public class GalleryController {

	private final PhotoService photoService;

	public GalleryController(PhotoService photoService) {
		this.photoService = photoService;
	}

	@GetMapping("/gallery")
	public String gallery(@RequestParam(required = false) String category, Model model) {
		PhotoCategory current = parseCategory(category); // 잘못된 값이면 null = 전체
		model.addAttribute("photos", photoService.findByCategory(current));
		model.addAttribute("categories", PhotoCategory.values());
		model.addAttribute("currentCategory", current);
		return "pages/gallery";
	}

	/** "NATURE" → PhotoCategory.NATURE, 없거나 이상한 값 → null (전체) */
	private PhotoCategory parseCategory(String value) {
		if (value == null || value.isBlank()) {
			return null;
		}
		try {
			return PhotoCategory.valueOf(value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}
}