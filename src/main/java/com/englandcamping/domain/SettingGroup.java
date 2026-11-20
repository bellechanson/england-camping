package com.englandcamping.domain;

import static com.englandcamping.domain.SettingKey.*;

import java.util.List;

/**
 * 관리자 "글/링크 설정" 화면의 탭 묶음. - 여기 들어 있는 항목만 화면에 보인다. - 띠 공지(BANNER_*)는 공지 관리 화면에
 * 따로 있고, 지도 좌표(MAP_LAT, MAP_LNG)는 잘못 고치면 지도가 깨져서 일부러 뺐다.
 */
public enum SettingGroup {

	INTRO("소개", "메인 화면과 소개 페이지에 나오는 글입니다.", List.of(CAMP_NAME, INTRO_TITLE, HERO_SUBTITLE, INTRO_BODY)),

	CONTACT("연락처·지도", "꼬리말과 오시는 길 페이지에 나오는 내용입니다.",
			List.of(ADDRESS_ROAD, ADDRESS_JIBUN, ZIP_CODE, PHONE, PARKING_INFO, MAP_NAVER_URL)),

	GUIDE("이용 안내", "이용안내 페이지에 나오는 시간과 규칙입니다.", List.of(CHECK_IN, CHECK_OUT, MANNER_TIME, FACILITY_CLOSED_INFO, RULES)),

	PRICE_TEXT("요금 글", "사이트·요금 페이지의 안내 글입니다. 금액 숫자는 '요금 관리'에서 고칩니다.",
			List.of(PEAK_SEASON_INFO, EXTRA_PERSON_INFO, LONG_STAY_INFO)),

	RESERVE("예약 링크", "예약 버튼이 연결될 주소입니다. 비워 두면 그 버튼은 홈페이지에서 사라집니다.",
			List.of(RESERVE_NAVER_URL, RESERVE_CAMFIT_URL, RESERVE_THANKYOU_URL));

	/** 탭 이름 */
	private final String label;
	/** 탭 위에 보여 줄 짧은 설명 */
	private final String description;
	/** 이 탭에서 고치는 항목들 (화면에 이 순서대로 나옴) */
	private final List<SettingKey> keys;

	SettingGroup(String label, String description, List<SettingKey> keys) {
		this.label = label;
		this.description = description;
		this.keys = keys;
	}

	/** 주소의 ?tab=CONTACT 같은 값 → 묶음. 모르는 값이면 첫 탭(소개) */
	public static SettingGroup from(String name) {
		for (SettingGroup group : values()) {
			if (group.name().equalsIgnoreCase(name)) {
				return group;
			}
		}
		return INTRO;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}

	public List<SettingKey> getKeys() {
		return keys;
	}
}