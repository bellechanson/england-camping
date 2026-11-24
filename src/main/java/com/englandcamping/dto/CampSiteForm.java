package com.englandcamping.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.format.annotation.DateTimeFormat;

import com.englandcamping.domain.CampSite;

import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

/**
 * 사이트 관리 화면 = 사이트 22개를 한 번에 받는 폼. 저장하는 건 "메모"와 "이용 불가 기간"뿐. 이용 가능/불가는 기간으로만 정해짐
 * (기간 안이면 불가, 끝나면 저절로 가능). 이름·종류는 화면 표시용(저장 안 함). 화면에서는 위치별로 묶어서 타일로 보여 줌
 * (getGroups)
 */
public class CampSiteForm {

	/**
	 * 사이트 위치별 묶음 (화면에 이 순서대로). 짧은 이름 = "01번 사이트" → "01", "A 사이트" → "A". 사이트를 옮기거나
	 * 늘리면 여기 목록만 고치면 됨 (목록에 없는 사이트는 "그 밖의 사이트"로 자동으로 들어감)
	 */
	private static final List<Area> AREAS = List.of(new Area("도로 근처", List.of("A", "B", "C", "D", "E")),
			new Area("집 근처", List.of("F", "G", "H")), new Area("가운데", List.of("13", "14")),
			new Area("산 쪽 (뒤로 산)", List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12")));

	/** @Valid: 줄마다 메모 글자 수·기간 검사 */
	@Valid
	private List<Row> rows = new ArrayList<>();

	/** 화면 처음 열 때: DB 사이트 목록으로 채우기 */
	public static CampSiteForm from(List<CampSite> sites) {
		CampSiteForm form = new CampSiteForm();
		LocalDate today = LocalDate.now();
		for (CampSite site : sites) {
			Row row = new Row();
			row.setId(site.getId());
			row.setName(site.getName());
			row.setTypeName(site.getSiteType().getName());
			row.setNote(site.getNote());
			// 이미 끝난 기간은 빈칸으로 (다음 저장 때 지워짐)
			boolean expired = site.getClosedUntil() != null && site.getClosedUntil().isBefore(today);
			if (!expired) {
				row.setClosedFrom(site.getClosedFrom());
				row.setClosedUntil(site.getClosedUntil());
			}
			// 예전 방식으로 꺼 둔 사이트(기간 없이 이용 불가) → "오늘부터 계속 불가"로 보여 줌. 저장하면 그대로 기간으로 바뀜
			if (!site.isAvailable() && row.getClosedFrom() == null && row.getClosedUntil() == null) {
				row.setClosedFrom(today);
			}
			form.getRows().add(row);
		}
		return form;
	}

	/**
	 * 화면용 위치 묶음: 묶음마다 rows 안의 번호(index) 목록. (저장할 때 rows[번호] 이름으로 보내야 해서 사이트 자체가 아니라
	 * 번호를 넘김)
	 */
	public List<Group> getGroups() {
		List<Group> groups = new ArrayList<>();
		Set<Integer> used = new HashSet<>();
		for (Area area : AREAS) {
			List<Integer> indexes = new ArrayList<>();
			for (String shortName : area.shortNames()) {
				for (int i = 0; i < rows.size(); i++) {
					if (shortName.equals(rows.get(i).getShortName())) {
						indexes.add(i);
						used.add(i);
					}
				}
			}
			if (!indexes.isEmpty()) {
				groups.add(new Group(area.label(), indexes));
			}
		}
		// 위 목록에 없는 사이트 (새로 생긴 사이트 등) → 빠지지 않게 맨 뒤에
		List<Integer> rest = new ArrayList<>();
		for (int i = 0; i < rows.size(); i++) {
			if (!used.contains(i)) {
				rest.add(i);
			}
		}
		if (!rest.isEmpty()) {
			groups.add(new Group("그 밖의 사이트", rest));
		}
		return groups;
	}

	public List<Row> getRows() {
		return rows;
	}

	public void setRows(List<Row> rows) {
		this.rows = rows;
	}

	/** 위치 묶음 정의 (이 파일 안에서만 씀) */
	private record Area(String label, List<String> shortNames) {
	}

	/** 화면용 묶음 하나 (이름표 + rows 번호 목록) */
	public static class Group {
		private final String label;
		private final List<Integer> indexes;

		public Group(String label, List<Integer> indexes) {
			this.label = label;
			this.indexes = indexes;
		}

		public String getLabel() {
			return label;
		}

		public List<Integer> getIndexes() {
			return indexes;
		}
	}

	/** 사이트 한 줄 */
	public static class Row {

		private Long id;

		/** 표시용 */
		private String name;

		/** 표시용 */
		private String typeName;

		@Size(max = 200, message = "메모는 200자 이내로 입력해 주세요.")
		private String note;

		/** 이용 불가 시작일 (비면 "지금부터") */
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		private LocalDate closedFrom;

		/** 이용 불가 끝나는 날 (비면 "끝 없이 계속") */
		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
		private LocalDate closedUntil;

		/** 기간 검사: 둘 다 있으면 시작일이 끝나는 날과 같거나 앞이어야 함 */
		@AssertTrue(message = "시작일이 끝나는 날보다 늦어요. 날짜를 확인해 주세요.")
		public boolean isPeriodValid() {
			return closedFrom == null || closedUntil == null || !closedFrom.isAfter(closedUntil);
		}

		/** 짧은 이름 ("01번 사이트" → "01") — 타일·메모 칸 이름, 위치 묶음 찾기 */
		public String getShortName() {
			return CampSite.shortNameOf(name);
		}

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

		public String getNote() {
			return note;
		}

		public void setNote(String note) {
			this.note = note;
		}

		public LocalDate getClosedFrom() {
			return closedFrom;
		}

		public void setClosedFrom(LocalDate closedFrom) {
			this.closedFrom = closedFrom;
		}

		public LocalDate getClosedUntil() {
			return closedUntil;
		}

		public void setClosedUntil(LocalDate closedUntil) {
			this.closedUntil = closedUntil;
		}
	}
}