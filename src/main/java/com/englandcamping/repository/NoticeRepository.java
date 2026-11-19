package com.englandcamping.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.Notice;

/** notice 테이블 조회/저장 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

	/** 목록: 고정 공지 먼저, 그다음 최신순 (페이지 나눔) */
	Page<Notice> findAllByOrderByPinnedDescCreatedAtDescIdDesc(Pageable pageable);

	/** 메인용: 같은 순서로 3개만 */
	List<Notice> findTop3ByOrderByPinnedDescCreatedAtDescIdDesc();
}