package com.englandcamping.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.support.RequestContextUtils;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 사진이 너무 커서(한 장 20MB / 한 번에 200MB 초과) 받기 전에 막힌 경우 오류 화면 대신 사진 관리 화면으로 돌아가서 빨간
 * 안내 띠로 알려 줌 (컨트롤러에 들어가기 전에 생기는 오류라 여기 따로 둠)
 */
@ControllerAdvice
public class UploadErrorAdvice {

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public String tooLarge(HttpServletRequest request) {
		RequestContextUtils.getOutputFlashMap(request).put("error", "사진이 너무 큽니다. 한 장은 20MB, 한 번에 200MB까지 올릴 수 있습니다.");
		return "redirect:/admin/photos";
	}
}