package com.englandcamping.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.englandcamping.domain.CampSite;
import com.englandcamping.domain.SiteType;
import com.englandcamping.dto.CampSiteForm;
import com.englandcamping.dto.SiteTypeForm;
import com.englandcamping.repository.CampSiteRepository;
import com.englandcamping.repository.SiteTypeRepository;

/** 사이트 안내 / 요금 페이지에서 쓸 데이터 읽기 + 관리자 요금·싸이트 저장 */
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

	/** 대표 사이트 종류 (지금은 하나뿐이라 첫 번째). 없으면 null */
	public SiteType findMainType() {
		List<SiteType> types = findAllTypes();
		return types.isEmpty() ? null : types.get(0);
	}

	/** 사이트 종류 하나 (없으면 404 페이지) */
	public SiteType getType(Long id) {
		return siteTypeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사이트 종류를 찾을 수 없습니다."));
	}

	/** 관리자: 요금(사이트 종류) 수정. 보여 주는 순서(sortOrder)는 화면에서 안 받으므로 그대로 둔다 */
	@Transactional
	public void updateType(Long id, SiteTypeForm form) {
		SiteType type = getType(id);
		type.update(form.getName().strip(), blankToNull(form.getFloorType()), blankToNull(form.getSizeText()),
				form.getBaseGuests(), form.getMaxGuests(), form.getNormalPrice(), form.getPeakPrice(),
				form.getExtraPersonFee(), form.getMinNights(), type.getSortOrder());
	}

	/**
	 * 관리자: 싸이트 이용 가능/불가 + 메모 한꺼번에 저장. 이름·종류·순서는 건드리지 않는다.
	 *
	 * @return 실제로 바뀐 싸이트 수 (안내 문구용)
	 */
	@Transactional
	public int updateSiteStatuses(List<CampSiteForm.Row> rows) {
		Map<Long, CampSite> sites = campSiteRepository.findAllByOrderBySortOrderAscIdAsc().stream()
				.collect(Collectors.toMap(CampSite::getId, Function.identity()));

		int changed = 0;
		for (CampSiteForm.Row row : rows) {
			CampSite site = sites.get(row.getId());
			if (site == null) {
				continue; // 없는 번호가 들어오면 무시
			}
			String note = blankToNull(row.getNote());
			// 바뀐 게 없으면 건너뜀
			if (site.isAvailable() == row.isAvailable() && Objects.equals(site.getNote(), note)) {
				continue;
			}
			site.update(site.getName(), site.getSiteType(), row.isAvailable(), note, site.getSortOrder());
			changed++;
		}
		return changed;
	}

	/** 앞뒤 공백 정리, 비었으면 null */
	private static String blankToNull(String value) {
		if (value == null) {
			return null;
		}
		String stripped = value.strip();
		return stripped.isEmpty() ? null : stripped;
	}
}