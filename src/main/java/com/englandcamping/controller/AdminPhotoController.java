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

import com.englandcamping.domain.PhotoCategory;
import com.englandcamping.domain.PhotoSlot;
import com.englandcamping.service.PhotoService;

/**
 * 관리자: 사진 올리기 / 분류·설명 / 순서 / 자리 지정 / 삭제 처리 후에는 항상 사진 관리 화면으로 돌아오고, 결과는 안내 띠로
 */
@Controller
@RequestMapping("/admin/photos")
public class AdminPhotoController {

	private static final String REDIRECT = "redirect:/admin/photos";

	private final PhotoService photoService;

	public AdminPhotoController(PhotoService photoService) {
		this.photoService = photoService;
	}

	/** 사진 관리 화면 */
	@GetMapping
	public String list(Model model) {
		model.addAttribute("photos", photoService.findAll());
		model.addAttribute("categories", PhotoCategory.values());
		model.addAttribute("slots", PhotoSlot.values());
		model.addAttribute("heroSlides", photoService.findHeroSlides()); // 메인 큰 사진 1~4: 폰 위치 정하기
		return "admin/photos";
	}

	/** 여러 장 올리기 */
	@PostMapping
	public String upload(@RequestParam(value = "files", required = false) List<MultipartFile> files,
			@RequestParam(required = false) PhotoCategory category, RedirectAttributes redirect) {
		try {
			int count = photoService.upload(files, category);
			redirect.addFlashAttribute("message", "사진 " + count + "장을 올렸습니다.");
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return REDIRECT;
	}

	/** 분류, 설명 저장 */
	@PostMapping("/{id}")
	public String updateInfo(@PathVariable Long id, @RequestParam(required = false) PhotoCategory category,
			@RequestParam(defaultValue = "") String caption, RedirectAttributes redirect) {
		try {
			photoService.updateInfo(id, category, caption);
			redirect.addFlashAttribute("message", "사진 정보를 저장했습니다.");
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return REDIRECT;
	}

	/** 순서 한 칸 앞/뒤 (dir = up / down) */
	@PostMapping("/{id}/move")
	public String move(@PathVariable Long id, @RequestParam String dir) {
		photoService.move(id, "up".equals(dir));
		return REDIRECT; // 순서 바꾸기는 자주 누르니까 안내 띠 없이
	}

	/** 자리 지정 (photoId 없이 오면 그 자리 비우기) */
	@PostMapping("/slots")
	public String assignSlot(@RequestParam PhotoSlot slot, @RequestParam(required = false) Long photoId,
			RedirectAttributes redirect) {
		photoService.assignSlot(slot, photoId);
		redirect.addFlashAttribute("message", (photoId == null) ? "'" + slot.getLabel() + "' 자리를 비웠습니다."
				: "'" + slot.getLabel() + "' 자리에 사진을 넣었습니다.");
		return REDIRECT;
	}

	/** 메인 큰 사진: 폰에서 보일 가로 위치 저장 (관리자 화면에서 틀을 끌어서 정한 0~100) */
	@PostMapping("/slots/focus")
	public String changeFocus(@RequestParam PhotoSlot slot, @RequestParam int focusX, RedirectAttributes redirect) {
		try {
			photoService.changeFocusX(slot, focusX);
			redirect.addFlashAttribute("message", "'" + slot.getLabel() + "'의 폰 화면 위치를 저장했습니다.");
		} catch (IllegalArgumentException e) {
			redirect.addFlashAttribute("error", e.getMessage());
		}
		return REDIRECT;
	}

	/** 삭제 (확인 창에서 "삭제"를 눌렀을 때만) */
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, RedirectAttributes redirect) {
		photoService.delete(id);
		redirect.addFlashAttribute("message", "사진을 삭제했습니다.");
		return REDIRECT;
	}
}