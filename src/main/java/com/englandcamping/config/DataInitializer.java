package com.englandcamping.config;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.AdminUser;
import com.englandcamping.domain.CampSite;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.domain.SiteSetting;
import com.englandcamping.domain.SiteType;
import com.englandcamping.repository.AdminUserRepository;
import com.englandcamping.repository.CampSiteRepository;
import com.englandcamping.repository.SiteSettingRepository;
import com.englandcamping.repository.SiteTypeRepository;

/**
 * 앱이 켜질 때 한 번 실행. - site_setting: DB에 없는 항목만 기본값으로 추가 (이미 고친 값은 건드리지 않음) -
 * site_type / camp_site: 테이블이 완전히 비어 있을 때만 처음 데이터 추가 - admin_user: 관리자가 한 명도 없을
 * 때만, 환경 변수 비밀번호로 만듦 → 몇 번을 재시작해도 데이터가 중복되지 않는다.
 */
@Component
public class DataInitializer implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

	private final SiteSettingRepository siteSettingRepository;
	private final SiteTypeRepository siteTypeRepository;
	private final CampSiteRepository campSiteRepository;
	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;
	private final String initialAdminUsername;
	private final String initialAdminPassword;

	public DataInitializer(SiteSettingRepository siteSettingRepository, SiteTypeRepository siteTypeRepository,
			CampSiteRepository campSiteRepository, AdminUserRepository adminUserRepository,
			PasswordEncoder passwordEncoder, @Value("${admin.initial-username:admin}") String initialAdminUsername,
			@Value("${admin.initial-password:}") String initialAdminPassword) {
		this.siteSettingRepository = siteSettingRepository;
		this.siteTypeRepository = siteTypeRepository;
		this.campSiteRepository = campSiteRepository;
		this.adminUserRepository = adminUserRepository;
		this.passwordEncoder = passwordEncoder;
		this.initialAdminUsername = initialAdminUsername;
		this.initialAdminPassword = initialAdminPassword;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		int addedSettings = initSettings();
		int addedTypes = 0;
		int addedSites = 0;

		// 사이트 종류와 사이트는 "둘 다 비어 있을 때"만 같이 만든다
		if (siteTypeRepository.count() == 0 && campSiteRepository.count() == 0) {
			SiteType basic = siteTypeRepository.save(createBasicType());
			addedTypes = 1;
			addedSites = initSites(basic);
		}

		String adminStatus = initAdmin();

		log.info("[DataInitializer] 글/링크 {}개, 사이트 종류 {}개, 사이트 {}개 추가 / 관리자 계정: {}", addedSettings, addedTypes,
				addedSites, adminStatus);
	}

	/** 글/링크: enum에 있는데 DB에 없는 것만 추가 */
	private int initSettings() {
		int added = 0;
		for (SettingKey key : SettingKey.values()) {
			if (!siteSettingRepository.existsById(key.name())) {
				siteSettingRepository.save(new SiteSetting(key.name(), key.getDefaultValue()));
				added++;
			}
		}
		return added;
	}

	/**
	 * 지금 운영 중인 사이트 종류 (전 사이트 동일) 파쇄석 8m x 10m / 기준 2인, 최대 5인 / 평소 50,000원, 연휴
	 * 60,000원 추가 1인 10,000원 / 최소 1박
	 */
	private SiteType createBasicType() {
		return new SiteType("캠핑사이트", "파쇄석", "8m x 10m", 2, 5, 50000, 60000, 10000, 1, 1);
	}

	/**
	 * 사이트 22개: 01번 ~ 14번 다음에 A ~ H 순서 08번은 장박 이용 중이라 막아 둠 (관리자 화면에서 다시 열 수 있음)
	 */
	private int initSites(SiteType type) {
		List<CampSite> sites = new ArrayList<>();
		int order = 1;

		// 숫자 사이트: 01번 ~ 14번
		for (int i = 1; i <= 14; i++) {
			String name = String.format("%02d번 사이트", i);
			if (i == 8) {
				sites.add(new CampSite(name, type, false, "장박 이용 중 (2027년 3월까지)", order++));
			} else {
				sites.add(new CampSite(name, type, true, null, order++));
			}
		}

		// 알파벳 사이트: A ~ H
		for (char c = 'A'; c <= 'H'; c++) {
			sites.add(new CampSite(c + " 사이트", type, true, null, order++));
		}

		campSiteRepository.saveAll(sites);
		return sites.size();
	}

	/**
	 * 관리자 계정: 한 명도 없을 때만 만든다. 비밀번호 환경 변수가 비어 있으면 만들지 않고 경고만 남김 (빈 비밀번호 계정 방지) ※
	 * 비밀번호 값은 절대 로그에 찍지 않는다
	 */
	private String initAdmin() {
		if (adminUserRepository.count() > 0) {
			return "이미 있음";
		}
		if (initialAdminPassword == null || initialAdminPassword.isBlank()) {
			log.warn("[DataInitializer] 관리자 계정이 없습니다. 환경 변수 ENGLAND_CAMPING_ADMIN_PASSWORD 를 설정하고 다시 실행하세요.");
			return "없음 (비밀번호 환경 변수 필요)";
		}
		String username = initialAdminUsername.strip();
		adminUserRepository.save(new AdminUser(username, passwordEncoder.encode(initialAdminPassword)));
		return "새로 만듦 (아이디: " + username + ")";
	}
}