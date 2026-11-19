package com.englandcamping.domain;

/**
 * 관리자가 화면에서 바꿀 수 있는 글/링크 목록. - 항목이 더 필요하면 여기에 한 줄만 추가하면 된다. (앱이 켜질 때
 * DataInitializer가 DB에 없는 항목을 기본값으로 자동 추가함) - 기본값 = 부모님이 네이버/캠핏/땡큐캠핑에 적어 둔 내용
 * 기준 - 금액(1박 요금, 추가 인원 요금)은 여기 말고 SiteType에 숫자로 저장
 */
public enum SettingKey {

	// ===== 소개 =====
	CAMP_NAME("캠핑장 이름", "잉글랜드 캠핑장"), INTRO_TITLE("메인 소개 제목", "공기 좋고 물 맑은 가평 북면의 조용한 캠핑장"),
	INTRO_BODY("소개 본문",
			"""
					저희 캠핑장은 캠지기 부부가 다년간 캠핑을 다닌 경험을 바탕으로 각 싸이트 및 관리동의 부대시설을 캠지기가 직접 설계 및 직영 시공하여, 이용 고객들의 불편함을 최대한 줄이고 안락하고 편안히 쉴 수 있는 공간으로 만들고자 노력하였습니다.

					깨끗하고 청결한 남녀 화장실과 개수대, 일반 캠핑장에서는 경험하기 힘든 프라이빗 샤워실과 탈의실. 고객분들이 사용하시는 부대시설에 불편함이 없도록 최대한 노력하였습니다.

					캠핑장 각 싸이트는 최대한 넓게 배치하였으며, 각 싸이트 앞에 주차를 하실 수도 있고 별도의 주차 공간에 주차를 하실 수도 있습니다.

					신생 캠핑장이지만 최대한 큰 나무와 많은 수의 나무를 심어 신생 캠핑장답지 않은 그늘이 만들어지며, 여름이면 캠핑장 앞 계곡에서 아이들과 물놀이도 가능하고, 가을이면 산속 캠핑장답게 예쁜 단풍이 들어 아름다운 추억을 만들어 가실 수 있습니다.

					공기 좋고 물 맑고 산세 좋은 조용한 곳에 위치한 잉글랜드 캠핑장에서 최고의 휴양을 만끽해 보시기 바랍니다."""),

	// ===== 연락처 / 오시는 길 =====
	ADDRESS_ROAD("주소 (도로명)", "경기 가평군 북면 멱골로 265"), ADDRESS_JIBUN("주소 (지번)", "경기 가평군 북면 목동리 328-1"),
	ZIP_CODE("우편번호", "12403"), PHONE("전화번호", "010-3665-2093"),
	PARKING_INFO("주차 안내", "무료 주차. 각 싸이트 앞 또는 별도 주차 공간에 주차하실 수 있습니다."),

	// ===== 이용 안내 =====
	CHECK_IN("체크인", "13:00"), CHECK_OUT("체크아웃", "12:00"), MANNER_TIME("매너타임", "22:30 ~ 07:00"),
	FACILITY_CLOSED_INFO("시설 이용 제한 안내",
			"개수대, 샤워장, 매점은 22:30 ~ 07:00에 이용하실 수 없습니다. 늦은 밤 물기 때문에 미끄러지는 등 안전사고를 막기 위한 것이니 양해 부탁드립니다."),
	RULES("이용 규칙 (한 줄에 하나씩)", """
			반려견 및 애완동물은 입장하실 수 없습니다.
			텐트 길이는 10m 이내만 가능합니다.
			두 가족이 함께 이용하시는 경우에도 매너타임을 꼭 지켜 주세요."""),

	// ===== 요금 관련 글 (금액 숫자는 SiteType에) =====
	PEAK_SEASON_INFO("성수기 안내", "공휴일이 겹치는 연휴 기간에는 성수기 요금이 적용됩니다."),
	EXTRA_PERSON_INFO("추가 인원 안내", "16세 이하 어린이는 추가 요금이 없습니다."),
	LONG_STAY_INFO("장박 안내", "장박은 반드시 전화로 먼저 상담 후 결정됩니다. 월 260,000원 기준이며, 결제 방법(카드/현금)에 따라 금액이 조금 달라질 수 있습니다."),

	// ===== 예약 링크 (비어 있으면 화면에서 "준비 중"으로 표시할 예정) =====
	RESERVE_CAMFIT_URL("캠핏 예약 주소", "https://camfit.co.kr/camp/67d119ab902902001d592ba1"),
	RESERVE_THANKYOU_URL("땡큐캠핑 예약 주소", "https://m.thankqcamping.com/resv/view.hbb?cseq=14423"),
	RESERVE_NAVER_URL("네이버 예약 주소", "https://map.naver.com/p/entry/place/1171966289?placePath=%2Froom");

	/** 관리자 화면에 보여 줄 이름 */
	private final String label;
	/** DB에 값이 없을 때 쓸 기본값 */
	private final String defaultValue;

	SettingKey(String label, String defaultValue) {
		this.label = label;
		this.defaultValue = defaultValue;
	}

	public String getLabel() {
		return label;
	}

	public String getDefaultValue() {
		return defaultValue;
	}
}