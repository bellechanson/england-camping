package com.englandcamping.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 사이트 "종류" = 요금이 같은 묶음. 지금은 "캠핑사이트(파쇄석)" 하나뿐이지만, 나중에 데크나 카라반 같은 다른 종류가 생겨도 행만
 * 추가하면 된다. 금액이 비어 있으면(null) 화면에서 "문의"로 보여 줄 예정.
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

	/** 기준 인원 */
	private int baseGuests;

	/** 최대 인원 */
	private int maxGuests;

	/** 평소 1박 요금 (원) */
	private Integer normalPrice;

	/** 성수기(연휴) 1박 요금 (원) */
	private Integer peakPrice;

	/** 기준 인원 초과 시 1인당 추가 요금 (원) */
	private Integer extraPersonFee;

	/** 최소 숙박일 */
	private int minNights;

	/** 화면에 보여 줄 순서 (작을수록 위) */
	@Column(nullable = false)
	private int sortOrder;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected SiteType() {
	}

	public SiteType(String name, String floorType, String sizeText, int baseGuests, int maxGuests, Integer normalPrice,
			Integer peakPrice, Integer extraPersonFee, int minNights, int sortOrder) {
		this.name = name;
		this.floorType = floorType;
		this.sizeText = sizeText;
		this.baseGuests = baseGuests;
		this.maxGuests = maxGuests;
		this.normalPrice = normalPrice;
		this.peakPrice = peakPrice;
		this.extraPersonFee = extraPersonFee;
		this.minNights = minNights;
		this.sortOrder = sortOrder;
	}

	/** 내용 한 번에 바꾸기 (5단계 관리자 화면에서 사용) */
	public void update(String name, String floorType, String sizeText, int baseGuests, int maxGuests,
			Integer normalPrice, Integer peakPrice, Integer extraPersonFee, int minNights, int sortOrder) {
		this.name = name;
		this.floorType = floorType;
		this.sizeText = sizeText;
		this.baseGuests = baseGuests;
		this.maxGuests = maxGuests;
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

	public int getBaseGuests() {
		return baseGuests;
	}

	public int getMaxGuests() {
		return maxGuests;
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