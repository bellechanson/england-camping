package com.englandcamping.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.SettingGroup;
import com.englandcamping.domain.SettingKey;
import com.englandcamping.domain.SiteSetting;
import com.englandcamping.repository.SiteSettingRepository;

/**
 * 홈페이지 글/링크 읽기·저장. 화면(Thymeleaf)에서는 ${settings['PHONE']} 처럼 꺼내 쓴다.
 */
@Service
@Transactional(readOnly = true)
public class SiteSettingService {

	/** 전화번호: 숫자로 시작·끝, 중간에 숫자/-/공백 (tel: 링크에 쓰이므로 다른 글자 막음) */
	private static final Pattern PHONE_PATTERN = Pattern.compile("[0-9][0-9\\- ]{6,18}[0-9]");
	/** 우편번호: 숫자 5자리 */
	private static final Pattern ZIP_PATTERN = Pattern.compile("\\d{5}");

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

	/**
	 * 여러 줄짜리 글을 "줄 목록"으로. (빈 줄은 빼고, 앞뒤 공백 정리) - 소개 본문 → 문단 목록 - 이용 규칙 → 규칙 목록
	 */
	public List<String> getLines(SettingKey key) {
		String value = get(key);
		if (value == null || value.isBlank()) {
			return List.of();
		}
		return value.lines().map(String::strip).filter(line -> !line.isEmpty()).toList();
	}

	/**
	 * 값 바꾸기 (DB에 없으면 새로 만듦) — 띠 공지, 5단계 관리자 화면에서 사용 클래스 전체는 readOnly라서 여기만 쓰기 가능한
	 * 트랜잭션으로
	 */
	@Transactional
	public void update(SettingKey key, String value) {
		siteSettingRepository.findById(key.name()).ifPresentOrElse(setting -> setting.changeValue(value),
				() -> siteSettingRepository.save(new SiteSetting(key.name(), value)));
	}

	/**
	 * 관리자 "글/링크 설정" 화면에서 탭(묶음) 하나 저장. - 묶음 안의 항목을 먼저 전부 검사하고, 하나라도 틀리면 아무것도 저장하지
	 * 않는다. - 화면에서 안 넘어온 항목(값이 null)은 건드리지 않는다.
	 *
	 * @param submitted 화면에서 넘어온 값 (이름표 → 값, _csrf 같은 다른 값이 섞여 있어도 무시)
	 * @throws IllegalArgumentException 검사에 걸리면 (메시지를 그대로 화면 안내 띠에 보여 줌)
	 */
	@Transactional
	public void updateGroup(SettingGroup group, Map<String, String> submitted) {
		Map<SettingKey, String> cleaned = new LinkedHashMap<>();
		for (SettingKey key : group.getKeys()) {
			String raw = submitted.get(key.name());
			if (raw == null) {
				continue;
			}
			String value = clean(key, raw);
			validate(key, value);
			cleaned.put(key, value);
		}
		// 여기까지 왔으면 전부 통과 → 한꺼번에 저장
		cleaned.forEach(this::update);
	}

	/** 입력값 정리: 줄 바꿈 통일, 앞뒤 공백 제거 */
	private static String clean(SettingKey key, String raw) {
		// Windows 브라우저는 줄 바꿈을 \r\n 으로 보냄 → \n 하나로 통일
		String value = raw.replace("\r\n", "\n").replace('\r', '\n');
		if (key.isMultiline()) {
			// 줄마다 앞뒤 공백만 정리 (문단 사이 빈 줄은 그대로 둠)
			return value.lines().map(String::strip).collect(Collectors.joining("\n")).strip();
		}
		// 한 줄 / 한 문단 항목: 줄 바꿈은 띄어쓰기로, 여러 칸 공백은 한 칸으로
		return value.replace('\n', ' ').replaceAll("\\s{2,}", " ").strip();
	}

	/** 항목 하나 검사. 틀리면 화면에 보여 줄 문구와 함께 예외 */
	private static void validate(SettingKey key, String value) {
		String label = "'" + key.getLabel() + "'";

		if (key.isRequired() && value.isEmpty()) {
			throw new IllegalArgumentException(label + " 칸은 비울 수 없습니다.");
		}
		if (value.length() > key.getMaxLength()) {
			throw new IllegalArgumentException(
					label + " 칸은 " + key.getMaxLength() + "자 이내로 입력해 주세요. (지금 " + value.length() + "자)");
		}
		if (value.isEmpty()) {
			return; // 비워도 되는 항목
		}
		if (key.isUrl()) {
			checkUrl(label, value);
		}
		if (key == SettingKey.PHONE && !PHONE_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("전화번호는 숫자와 - 만 넣어 주세요. (예: 010-1234-5678)");
		}
		if (key == SettingKey.ZIP_CODE && !ZIP_PATTERN.matcher(value).matches()) {
			throw new IllegalArgumentException("우편번호는 숫자 5자리로 넣어 주세요.");
		}
	}

	/**
	 * 링크 검사: http:// 또는 https:// 로 시작하고 사이트 주소(호스트)가 있어야 통과 (javascript: 같은 것 차단)
	 */
	private static void checkUrl(String label, String value) {
		try {
			URI uri = new URI(value);
			String scheme = uri.getScheme();
			boolean httpOrHttps = "http".equalsIgnoreCase(scheme) || "https".equalsIgnoreCase(scheme);
			if (httpOrHttps && uri.getHost() != null) {
				return;
			}
		} catch (URISyntaxException e) {
			// 주소 모양이 아님 → 아래 안내 문구로
		}
		throw new IllegalArgumentException(label + " 칸에는 http:// 또는 https:// 로 시작하는 인터넷 주소만 넣을 수 있습니다.");
	}
}