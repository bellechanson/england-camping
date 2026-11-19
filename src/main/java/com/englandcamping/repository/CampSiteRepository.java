package com.englandcamping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.CampSite;

/** camp_site 테이블 조회/저장 */
public interface CampSiteRepository extends JpaRepository<CampSite, Long> {

	/**
	 * 보여 줄 순서대로 전부.
	 * 
	 * @EntityGraph: 싸이트를 가져올 때 종류(siteType)도 한 번에 같이 가져옴 → 싸이트 22개마다 종류를 따로 조회하는
	 *               일(쿼리 22번)을 막는다.
	 */
	@EntityGraph(attributePaths = "siteType")
	List<CampSite> findAllByOrderBySortOrderAscIdAsc();
}