package com.englandcamping.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.SiteSetting;

/** site_setting 테이블 조회/저장. 기본키가 문자열(setting_key)이라 <SiteSetting, String> */
public interface SiteSettingRepository extends JpaRepository<SiteSetting, String> {
}