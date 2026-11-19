package com.englandcamping.domain;

/**
 * 사진 "자리" — 지금 회색 그라데이션으로 비어 있는 곳들
 * 관리자가 자리마다 사진 한 장씩 골라 넣는다. (안 고르면 지금처럼 그라데이션)
 */
public enum PhotoSlot {

    HERO("메인 큰 사진", "메인 첫 화면 배경 (가로 사진 추천)"),
    ABOUT_MAIN("소개 큰 사진", "메인 소개 칸 + 소개 페이지 (세로 사진 추천)"),
    ABOUT_SUB1("소개 작은 사진 1", "메인 소개 칸 + 소개 페이지 (정사각형으로 잘려 보임)"),
    ABOUT_SUB2("소개 작은 사진 2", "메인 소개 칸 + 소개 페이지 (정사각형으로 잘려 보임)"),
    SITE("사이트 대표 사진", "메인 사이트 카드 + 사이트·요금 페이지");

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