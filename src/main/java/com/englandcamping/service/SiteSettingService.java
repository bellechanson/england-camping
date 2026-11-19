package com.englandcamping.service;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.SettingKey;
import com.englandcamping.domain.SiteSetting;
import com.englandcamping.repository.SiteSettingRepository;

/**
 * 홈페이지 글/링크 읽기. 화면(Thymeleaf)에서는 ${settings['PHONE']} 처럼 꺼내 쓸 예정.
 */
@Service
@Transactional(readOnly = true)
public class SiteSettingService {

	private final SiteSettingRepository siteSettingRepository;

	public SiteSettingService(SiteSettingRepository siteSettingRepository) {
		this.siteSettingRepository = siteSettingRepository;
	}

	/**
	 * 모든 항목을 Map(이름표 → 값)으로. 1) 먼저 enum 기본값을 깔고 2) DB에 있는 값으로 덮어쓴다 → DB에 아직 없는 항목이
	 * 있어도 화면이 깨지지 않는다.
	 */
	public Map<String, String> getAllAsMap() {
		Map<String, String> map = new LinkedHashMap<>();
		for (SettingKey key : SettingKey.values()) {
			map.put(key.name(), key.getDefaultValue());
		}
		for (SiteSetting setting : siteSettingRepository.findAll()) {
			map.put(setting.getSettingKey(), setting.getSettingValue());
		}
		return map;
	}

	/** 항목 하나만 (없으면 기본값) */
	public String get(SettingKey key) {
		return siteSettingRepository.findById(key.name()).map(SiteSetting::getSettingValue)
				.orElse(key.getDefaultValue());
	}
}