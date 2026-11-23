package com.englandcamping.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 사이트 "종류" = 요금이 같은 묶음. 지금은 "캠핑사이트(파쇄석)" 하나뿐이지만, 나중에 데크나 카라반 같은 다른 종류가 생겨도 행만
 * 추가하면 된다. 금액이 비어 있으면(null) 화면에서 "문의"로 보여 줌.
 *
 * 인원 규칙 (2026-11 아빠 요청): 성인 = 16세 초과, 소인 = 16세 이하(인원 제한 없이 무료) - baseGuests =
 * 기준 성인 (예전 이름 "기준 인원" 칸을 그대로 씀) - maxGuests = 최대 성인 (예전 이름 "최대 인원" 칸을 그대로 씀) -
 * baseChildren = 기준 소인 (새 칸)
 */
@Entity
@Table(name = "site_type")
public class SiteType {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 종류 이름 (예: 캠핑사이트) */
	@Column(nullable = false, length = 50)
	private String name;

	/** 바닥 (예: 파쇄석) */
	@Column(length = 30)
	private String floorType;

	/** 크기 (예: 8m x 10m) — 숫자 계산할 일이 없어서 글자로 저장 */
	@Column(length = 30)
	private String sizeText;

	/** 기준 성인 (DB 칸 이름은 예전 그대로 base_guests) */
	private int baseGuests;

	/** 최대 성인 (DB 칸 이름은 예전 그대로 max_guests) */
	private int maxGuests;

	/** 기준 소인 — 새로 생긴 칸이라 예전 행은 null → getBaseChildren()에서 0 */
	private Integer baseChildren;

	/** 평소 1박 요금 (원) */
	private Integer normalPrice;

	/** 성수기(연휴) 1박 요금 (원) */
	private Integer peakPrice;

	/** 성인이 기준을 넘을 때 1인당 추가 요금 (원). 소인은 무료라 해당 없음 */
	private Integer extraPersonFee;

	/** 최소 숙박일 */
	private int minNights;

	/** 화면에 보여 줄 순서 (작을수록 위) */
	@Column(nullable = false)
	private int sortOrder;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected SiteType() {
	}

	public SiteType(String name, String floorType, String sizeText, int baseGuests, int maxGuests, int baseChildren,
			Integer normalPrice, Integer peakPrice, Integer extraPersonFee, int minNights, int sortOrder) {
		this.name = name;
		this.floorType = floorType;
		this.sizeText = sizeText;
		this.baseGuests = baseGuests;
		this.maxGuests = maxGuests;
		this.baseChildren = baseChildren;
		this.normalPrice = normalPrice;
		this.peakPrice = peakPrice;
		this.extraPersonFee = extraPersonFee;
		this.minNights = minNights;
		this.sortOrder = sortOrder;
	}

	/** 내용 한 번에 바꾸기 (관리자 요금 수정 화면에서 사용) */
	public void update(String name, String floorType, String sizeText, int baseGuests, int maxGuests, int baseChildren,
			Integer normalPrice, Integer peakPrice, Integer extraPersonFee, int minNights, int sortOrder) {
		this.name = name;
		this.floorType = floorType;
		this.sizeText = sizeText;
		this.baseGuests = baseGuests;
		this.maxGuests = maxGuests;
		this.baseChildren = baseChildren;
		this.normalPrice = normalPrice;
		this.peakPrice = peakPrice;
		this.extraPersonFee = extraPersonFee;
		this.minNights = minNights;
		this.sortOrder = sortOrder;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getFloorType() {
		return floorType;
	}

	public String getSizeText() {
		return sizeText;
	}

	/** 기준 성인 */
	public int getBaseGuests() {
		return baseGuests;
	}

	/** 최대 성인 */
	public int getMaxGuests() {
		return maxGuests;
	}

	/** 기준 소인 (값이 없으면 0) */
	public int getBaseChildren() {
		return (baseChildren == null) ? 0 : baseChildren;
	}

	public Integer getNormalPrice() {
		return normalPrice;
	}

	public Integer getPeakPrice() {
		return peakPrice;
	}

	public Integer getExtraPersonFee() {
		return extraPersonFee;
	}

	public int getMinNights() {
		return minNights;
	}

	public int getSortOrder() {
		return sortOrder;
	}
}