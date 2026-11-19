package com.englandcamping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.SiteType;

/** site_type 테이블 조회/저장 */
public interface SiteTypeRepository extends JpaRepository<SiteType, Long> {

	/** 보여 줄 순서대로 전부 */
	List<SiteType> findAllByOrderBySortOrderAscIdAsc();
}