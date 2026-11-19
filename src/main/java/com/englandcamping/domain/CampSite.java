package com.englandcamping.domain;

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
 * 개별 싸이트 하나 = 한 행. (01번 ~ 14번, A ~ H → 총 22개) - available = false 이면 손님 화면에서
 * "이용 불가"로 표시 (예: 장박 중인 08번) - note 는 관리자만 보는 메모 (손님 화면에는 안 보여 줄 예정)
 */
@Entity
@Table(name = "camp_site")
public class CampSite {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 싸이트 이름 (예: 01번 싸이트, A 싸이트) */
	@Column(nullable = false, length = 30)
	private String name;

	/** 어떤 종류(요금 묶음)에 속하는지 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "site_type_id", nullable = false)
	private SiteType siteType;

	/** 지금 손님이 이용 가능한지 */
	@Column(nullable = false)
	private boolean available;

	/** 관리자 메모 (예: 장박 이용 중, 2027년 3월까지) */
	@Column(length = 200)
	private String note;

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

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
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
}