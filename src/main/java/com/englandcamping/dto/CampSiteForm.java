package com.englandcamping.dto;

import java.util.ArrayList;
import java.util.List;

import com.englandcamping.domain.CampSite;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

/**
 * 싸이트 관리 화면 = 싸이트 22줄을 한 번에 받는 폼. 저장하는 건 "이용 가능 여부"와 "메모"뿐. 이름·종류는 화면 표시용(저장 안
 * 함).
 */
public class CampSiteForm {

	/** @Valid: 줄마다 메모 글자 수 검사 */
	@Valid
	private List<Row> rows = new ArrayList<>();

	/** 화면 처음 열 때: DB 싸이트 목록으로 채우기 */
	public static CampSiteForm from(List<CampSite> sites) {
		CampSiteForm form = new CampSiteForm();
		for (CampSite site : sites) {
			Row row = new Row();
			row.setId(site.getId());
			row.setName(site.getName());
			row.setTypeName(site.getSiteType().getName());
			row.setAvailable(site.isAvailable());
			row.setNote(site.getNote());
			form.getRows().add(row);
		}
		return form;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	/** 싸이트 한 줄 */
	public static class Row {

		private Long id;
		/** 표시용 */
		private String name;
		/** 표시용 */
		private String typeName;
		/** 체크 안 하면 false (Thymeleaf th:field가 숨은 칸을 같이 보내 줌) */
		private boolean available;

		@Size(max = 200, message = "메모는 200자 이내로 입력해 주세요.")
		private String note;

		public Long getId() {
			return id;
		}

		public void setId(Long id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public boolean isAvailable() {
			return available;
		}

		public void setAvailable(boolean available) {
			this.available = available;
		}

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}
	}
}