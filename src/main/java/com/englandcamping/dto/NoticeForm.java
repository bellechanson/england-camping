package com.englandcamping.dto;

import com.englandcamping.domain.Notice;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** 공지 쓰기/수정 화면에서 받는 값 (빈 칸이면 화면에 오류 문구) */
public class NoticeForm {

	@NotBlank(message = "제목을 입력해 주세요.")
	@Size(max = 100, message = "제목은 100자 이내로 입력해 주세요.")
	private String title;

	@NotBlank(message = "내용을 입력해 주세요.")
	private String content;

	/** 상단 고정 (체크 안 하면 false) */
	private boolean pinned;

	/** 수정 화면: 저장된 공지로 채우기 */
	public static NoticeForm from(Notice notice) {
		NoticeForm form = new NoticeForm();
		form.setTitle(notice.getTitle());
		form.setContent(notice.getContent());
		form.setPinned(notice.isPinned());
		return form;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}
}