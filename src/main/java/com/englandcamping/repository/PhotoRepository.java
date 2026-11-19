package com.englandcamping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.englandcamping.domain.Photo;
import com.englandcamping.domain.PhotoCategory;

/** photo 테이블 조회/저장 */
public interface PhotoRepository extends JpaRepository<Photo, Long> {

	/** 전체 (갤러리 순서대로) */
	List<Photo> findAllByOrderBySortOrderAscIdAsc();

	/** 분류 하나만 (갤러리 탭) */
	List<Photo> findByCategoryOrderBySortOrderAscIdAsc(PhotoCategory category);

	/** 메인 갤러리 칸용 앞에서 5장 */
	List<Photo> findTop5ByOrderBySortOrderAscIdAsc();

	/** 지금 가장 큰 순서 번호 (사진이 없으면 0) → 새 사진은 맨 뒤에 붙임 */
	@Query("select coalesce(max(p.sortOrder), 0) from Photo p")
	int findMaxSortOrder();
}