package com.englandcamping.dto;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.englandcamping.domain.Notice;
import com.englandcamping.domain.NoticeCategory;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

	/** 분류 (처음엔 "안내"로 골라져 있음) */
	@NotNull(message = "분류를 골라 주세요.")
	private NoticeCategory category = NoticeCategory.NOTICE;

	/** 게시 기간 (둘 다 비워도 됨). 화면의 날짜 칸(type="date")은 "2026-11-27" 모양으로 보냄 */
	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate startDate;

	@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
	private LocalDate endDate;

	/** 수정 화면: 저장된 공지로 채우기 */
	public static NoticeForm from(Notice notice) {
		NoticeForm form = new NoticeForm();
		form.setTitle(notice.getTitle());
		form.setContent(notice.getContent());
		form.setPinned(notice.isPinned());
		form.setCategory(notice.getCategory());
		form.setStartDate(notice.getStartDate());
		form.setEndDate(notice.getEndDate());
		return form;
	}

	/** 기간 검사: 둘 다 있으면 끝나는 날이 시작일보다 앞이면 안 됨 (화면에서 periodValid 오류로 보임) */
	@AssertTrue(message = "끝나는 날은 시작일과 같거나 그 뒤여야 합니다.")
	public boolean isPeriodValid() {
		return startDate == null || endDate == null || !endDate.isBefore(startDate);
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

	public NoticeCategory getCategory() {
		return category;
	}

	public void setCategory(NoticeCategory category) {
		this.category = category;
	}

	public LocalDate getStartDate() {
		return startDate;
	}

	public void setStartDate(LocalDate startDate) {
		this.startDate = startDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
}