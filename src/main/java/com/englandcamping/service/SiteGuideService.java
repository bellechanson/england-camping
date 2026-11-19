package com.englandcamping.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.CampSite;
import com.englandcamping.domain.SiteType;
import com.englandcamping.repository.CampSiteRepository;
import com.englandcamping.repository.SiteTypeRepository;

/** 사이트 안내 / 요금 페이지에서 쓸 데이터 읽기 */
@Service
@Transactional(readOnly = true)
public class SiteGuideService {

	private final SiteTypeRepository siteTypeRepository;
	private final CampSiteRepository campSiteRepository;

	public SiteGuideService(SiteTypeRepository siteTypeRepository, CampSiteRepository campSiteRepository) {
		this.siteTypeRepository = siteTypeRepository;
		this.campSiteRepository = campSiteRepository;
	}

	/** 사이트 종류(요금 묶음) 전체 */
	public List<SiteType> findAllTypes() {
		return siteTypeRepository.findAllByOrderBySortOrderAscIdAsc();
	}

	/** 개별 싸이트 전체 (종류 정보 포함) */
	public List<CampSite> findAllSites() {
		return campSiteRepository.findAllByOrderBySortOrderAscIdAsc();
	}
}