package com.englandcamping.dto;

import com.englandcamping.domain.SiteType;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 요금(사이트 종류) 수정 화면에서 받는 값. 금액 칸을 비우면 null → 손님 화면에 "전화 문의"로 나옴.
 */
public class SiteTypeForm {

	@NotBlank(message = "이름을 입력해 주세요.")
	@Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
	private String name;

	@Size(max = 30, message = "바닥은 30자 이내로 입력해 주세요.")
	private String floorType;

	@Size(max = 30, message = "크기는 30자 이내로 입력해 주세요.")
	private String sizeText;

	@NotNull(message = "기준 인원을 입력해 주세요.")
	@Min(value = 1, message = "기준 인원은 1명 이상이어야 합니다.")
	@Max(value = 50, message = "기준 인원이 너무 큽니다.")
	private Integer baseGuests;

	@NotNull(message = "최대 인원을 입력해 주세요.")
	@Min(value = 1, message = "최대 인원은 1명 이상이어야 합니다.")
	@Max(value = 50, message = "최대 인원이 너무 큽니다.")
	private Integer maxGuests;

	@Min(value = 0, message = "0원 이상으로 입력해 주세요.")
	@Max(value = 10_000_000, message = "금액이 너무 큽니다.")
	private Integer normalPrice;

	@Min(value = 0, message = "0원 이상으로 입력해 주세요.")
	@Max(value = 10_000_000, message = "금액이 너무 큽니다.")
	private Integer peakPrice;

	@Min(value = 0, message = "0원 이상으로 입력해 주세요.")
	@Max(value = 10_000_000, message = "금액이 너무 큽니다.")
	private Integer extraPersonFee;

	@NotNull(message = "최소 숙박일을 입력해 주세요.")
	@Min(value = 1, message = "최소 숙박은 1박 이상이어야 합니다.")
	@Max(value = 365, message = "최소 숙박일이 너무 큽니다.")
	private Integer minNights;

	/** 최대 인원 >= 기준 인원 (둘 중 하나라도 비었으면 위의 @NotNull이 잡으므로 여기선 통과) */
	@AssertTrue(message = "최대 인원은 기준 인원보다 적을 수 없습니다.")
	public boolean isGuestRangeValid() {
		if (baseGuests == null || maxGuests == null) {
			return true;
		}
		return maxGuests >= baseGuests;
	}

	/** 수정 화면: 저장된 값으로 채우기 */
	public static SiteTypeForm from(SiteType type) {
		SiteTypeForm form = new SiteTypeForm();
		form.setName(type.getName());
		form.setFloorType(type.getFloorType());
		form.setSizeText(type.getSizeText());
		form.setBaseGuests(type.getBaseGuests());
		form.setMaxGuests(type.getMaxGuests());
		form.setNormalPrice(type.getNormalPrice());
		form.setPeakPrice(type.getPeakPrice());
		form.setExtraPersonFee(type.getExtraPersonFee());
		form.setMinNights(type.getMinNights());
		return form;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFloorType() {
		return floorType;
	}

	public void setFloorType(String floorType) {
		this.floorType = floorType;
	}

	public String getSizeText() {
		return sizeText;
	}

	public void setSizeText(String sizeText) {
		this.sizeText = sizeText;
	}

	public Integer getBaseGuests() {
		return baseGuests;
	}

	public void setBaseGuests(Integer baseGuests) {
		this.baseGuests = baseGuests;
	}

	public Integer getMaxGuests() {
		return maxGuests;
	}

	public void setMaxGuests(Integer maxGuests) {
		this.maxGuests = maxGuests;
	}

	public Integer getNormalPrice() {
		return normalPrice;
	}

	public void setNormalPrice(Integer normalPrice) {
		this.normalPrice = normalPrice;
	}

	public Integer getPeakPrice() {
		return peakPrice;
	}

	public void setPeakPrice(Integer peakPrice) {
		this.peakPrice = peakPrice;
	}

	public Integer getExtraPersonFee() {
		return extraPersonFee;
	}

	public void setExtraPersonFee(Integer extraPersonFee) {
		this.extraPersonFee = extraPersonFee;
	}

	public Integer getMinNights() {
		return minNights;
	}

	public void setMinNights(Integer minNights) {
		this.minNights = minNights;
	}
}