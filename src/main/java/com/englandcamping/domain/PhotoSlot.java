package com.englandcamping.domain;

import java.util.List;

/**
 * 사진 "자리" — 지금 회색 그라데이션으로 비어 있는 곳들. 관리자가 자리마다 사진 한 장씩 골라 넣는다. (안 고르면 지금처럼
 * 그라데이션) 메인 큰 사진은 1~4: 2장 이상 넣으면 메인 첫 화면에서 5초마다 바뀜 (비운 자리는 건너뜀) (11-26) "사이트 대표
 * 사진(SITE)" 자리는 뺌 → 사이트 칸은 사진 배치도(fragments/sites :: map)가 대신함. enum 값을 지웠으니 DB
 * photo_slot 에 SITE 줄이 남아 있으면 안 됨 (배포 전에 서버·PC 둘 다 DELETE)
 */
public enum PhotoSlot {

	HERO("메인 큰 사진 1", "메인 첫 화면 배경 + 카톡 미리보기 (가로 사진 추천)"), HERO_2("메인 큰 사진 2", "메인 첫 화면에서 5초마다 바뀜 (비우면 건너뜀)"),
	HERO_3("메인 큰 사진 3", "메인 첫 화면에서 5초마다 바뀜 (비우면 건너뜀)"), HERO_4("메인 큰 사진 4", "메인 첫 화면에서 5초마다 바뀜 (비우면 건너뜀)"),
	ABOUT_MAIN("소개 큰 사진", "메인 소개 칸 + 소개 페이지 (세로 사진 추천)"), ABOUT_SUB1("소개 작은 사진 1", "메인 소개 칸 + 소개 페이지 (정사각형으로 잘려 보임)"),
	ABOUT_SUB2("소개 작은 사진 2", "메인 소개 칸 + 소개 페이지 (정사각형으로 잘려 보임)");

	/** 메인 큰 사진 자리들 (이 순서대로 돌아감) */
	public static final List<PhotoSlot> HEROES = List.of(HERO, HERO_2, HERO_3, HERO_4);

	private final String label;
	/** 관리자 화면에 보여 줄 설명 (어디에 나오는지) */
	private final String description;

	PhotoSlot(String label, String description) {
		this.label = label;
		this.description = description;
	}

	public String getLabel() {
		return label;
	}

	public String getDescription() {
		return description;
	}
}