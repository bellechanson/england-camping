package com.englandcamping.domain;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 개별 사이트 하나 = 한 행. (01번 ~ 14번, A ~ H → 총 22개) - available = false 이면 손님 화면에서
 * "이용 불가"로 표시 (예: 장박 중인 08번) - note 는 관리자만 보는 메모 (손님 화면에는 안 보여 줄 예정)
 */
@Entity
@Table(name = "camp_site")
public class CampSite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 사이트 이름 (예: 01번 사이트, A 사이트) */
	@Column(nullable = false, length = 30)
	private String name;

	/** 어떤 종류(요금 묶음)에 속하는지 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_type_id", nullable = false)
	private SiteType siteType;

	/**
	 * 예전 방식의 켜기/끄기. 지금은 저장할 때 항상 true 이고, 이용 불가는 아래 기간(closedFrom~closedUntil)으로 정함
	 */
	@Column(nullable = false)
	private boolean available;

	/** 관리자 메모 (예: 장박 이용 중, 2027년 3월까지) */
	@Column(length = 200)
	private String note;

	/** 이용 불가 기간 시작일 (비면 "지금부터"). 관리자 > 사이트 관리에서 정함 */
	@Column(name = "closed_from")
	private LocalDate closedFrom;

	/** 이용 불가 기간 끝나는 날 (이날까지 불가). 비면 기간 없음 */
	@Column(name = "closed_until")
	private LocalDate closedUntil;

	/** 화면에 보여 줄 순서 (작을수록 앞) */
	@Column(nullable = false)
	private int sortOrder;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected CampSite() {
	}

	public CampSite(String name, SiteType siteType, boolean available, String note, int sortOrder) {
		this.name = name;
		this.siteType = siteType;
		this.available = available;
		this.note = note;
		this.sortOrder = sortOrder;
	}

	/** 내용 한 번에 바꾸기 (관리자 화면에서 사용) */
	public void update(String name, SiteType siteType, boolean available, String note, int sortOrder) {
		this.name = name;
		this.siteType = siteType;
		this.available = available;
		this.note = note;
		this.sortOrder = sortOrder;
	}

	/** 이용 불가 기간 바꾸기 (둘 다 null 이면 기간 없음) */
	public void changeClosedPeriod(LocalDate closedFrom, LocalDate closedUntil) {
		this.closedFrom = closedFrom;
		this.closedUntil = closedUntil;
	}

	/**
	 * 그날이 이용 불가 기간 안인지. 둘 다 비면 기간 없음 / 시작일만 있으면 그날부터 계속 / 끝나는 날만 있으면 그날까지 계속 (관리자
	 * 화면 스크립트의 stateOf 와 같은 규칙)
	 */
	public boolean isClosedOn(LocalDate day) {
		if (closedFrom == null && closedUntil == null) {
			return false;
		}
		if (closedFrom != null && day.isBefore(closedFrom)) {
			return false;
		}
		return closedUntil == null || !day.isAfter(closedUntil);
	}

	/**
	 * 손님 화면용: 지금 실제로 이용 가능한지 = 켜져 있고 + 오늘이 불가 기간이 아님. 화면을 열 때마다 오늘 날짜로 판단 → 기간이 끝나면
	 * 저절로 이용 가능 (밤에 따로 도는 작업 없음)
	 */
	public boolean isOpenNow() {
		return available && !isClosedOn(LocalDate.now());
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	/**
	 * 짧은 이름: "01번 사이트" → "01", "A 사이트" → "A" (손님 화면 네모 칸에 씀) "싸이트"로 된 옛날 데이터도 같이 처리
	 */
	public String getShortName() {
		return shortNameOf(name);
	}

	/** 이름 → 짧은 이름 (관리자 사이트 관리 화면에서도 같은 규칙으로 쓰려고 static) */
	public static String shortNameOf(String name) {
		if (name == null) {
			return "";
		}
		return name.replaceFirst("\\s*[사싸]이트$", "").replaceFirst("번$", "").strip();
	}

	public SiteType getSiteType() {
		return siteType;
	}

	public boolean isAvailable() {
		return available;
	}

	public String getNote() {
		return note;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public LocalDate getClosedFrom() {
		return closedFrom;
	}

	public LocalDate getClosedUntil() {
		return closedUntil;
	}
}