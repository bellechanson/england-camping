package com.englandcamping.dto;

import com.englandcamping.domain.PhotoSlot;

/**
 * 메인 큰 사진 한 장 (메인 첫 화면 돌아가기 + 관리자 "폰에서 보일 부분") 화면에서는 record 라서 ${s.url()} 처럼
 * 괄호를 붙여 꺼냄
 *
 * @param slot     자리 (HERO, HERO_2 ...)
 * @param url      큰 사진 주소
 * @param thumbUrl 미리보기 주소 (관리자 화면용)
 * @param focusX   폰에서 보일 가로 위치 0~100
 */
public record HeroSlide(PhotoSlot slot, String url, String thumbUrl, int focusX) {

	/** "메인 큰 사진 2" 같은 이름 */
	public String label() {
		return slot.getLabel();
	}

	/** CSS object-position 값 (예: "30% 50%") */
	public String objectPosition() {
		return focusX + "% 50%";
	}
}