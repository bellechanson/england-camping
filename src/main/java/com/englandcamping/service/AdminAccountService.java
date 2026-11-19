package com.englandcamping.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.AdminUser;
import com.englandcamping.repository.AdminUserRepository;

/** 관리자 계정 관리 (비밀번호 변경) */
@Service
@Transactional
public class AdminAccountService {

	/** 새 비밀번호 최소 길이 */
	public static final int MIN_PASSWORD_LENGTH = 8;

	private final AdminUserRepository adminUserRepository;
	private final PasswordEncoder passwordEncoder;

	public AdminAccountService(AdminUserRepository adminUserRepository, PasswordEncoder passwordEncoder) {
		this.adminUserRepository = adminUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	/**
	 * 비밀번호 변경 실패하면 IllegalArgumentException (메시지를 화면에 그대로 보여 줄 예정)
	 */
	public void changePassword(String username, String currentPassword, String newPassword) {
		AdminUser admin = adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new IllegalStateException("관리자 계정을 찾을 수 없습니다."));

		if (currentPassword == null || !passwordEncoder.matches(currentPassword, admin.getPassword())) {
			throw new IllegalArgumentException("현재 비밀번호가 맞지 않습니다.");
		}
		if (newPassword == null || newPassword.length() < MIN_PASSWORD_LENGTH) {
			throw new IllegalArgumentException("새 비밀번호는 " + MIN_PASSWORD_LENGTH + "자 이상이어야 합니다.");
		}
		if (passwordEncoder.matches(newPassword, admin.getPassword())) {
			throw new IllegalArgumentException("지금 쓰는 비밀번호와 다른 비밀번호를 입력해 주세요.");
		}

		admin.changePassword(passwordEncoder.encode(newPassword));
	}
}