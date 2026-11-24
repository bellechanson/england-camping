package com.englandcamping.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.englandcamping.domain.Notice;

/** notice 테이블 조회/저장 */
public interface NoticeRepository extends JpaRepository<Notice, Long> {

	/**
	 * 목록 순서: "지금 고정"인 공지 먼저 → 그다음 최신순. 고정이어도 게시 기간(endDate)이 오늘 이전이면 고정이 아닌 것으로 봄.
	 * 공지 목록(페이지 나눔), 메인·상세의 최근 3개(PageRequest.of(0, 3))에서 같이 씀
	 */
	@Query(value = """
			select n from Notice n
			order by
			  case when n.pinned = true and (n.endDate is null or n.endDate >= :today) then 0 else 1 end,
			  n.createdAt desc, n.id desc
			""", countQuery = "select count(n) from Notice n")
	Page<Notice> findPinnedFirst(@Param("today") LocalDate today, Pageable pageable);

	/** 이전 글: 이 번호보다 작은 것 중 가장 큰 번호 (= 바로 전에 쓴 글) */
	Optional<Notice> findFirstByIdLessThanOrderByIdDesc(Long id);

	/** 다음 글: 이 번호보다 큰 것 중 가장 작은 번호 (= 바로 다음에 쓴 글) */
	Optional<Notice> findFirstByIdGreaterThanOrderByIdAsc(Long id);
}