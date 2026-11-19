package com.englandcamping.repository;

import java.util.List;
import java.util.Optional;

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

	/** 이전 글: 이 번호보다 작은 것 중 가장 큰 번호 (= 바로 전에 쓴 글) */
	Optional<Notice> findFirstByIdLessThanOrderByIdDesc(Long id);

	/** 다음 글: 이 번호보다 큰 것 중 가장 작은 번호 (= 바로 다음에 쓴 글) */
	Optional<Notice> findFirstByIdGreaterThanOrderByIdAsc(Long id);
}