package com.englandcamping.config;

import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.AdminUser;
import com.englandcamping.repository.AdminUserRepository;

/**
 * 로그인 실패 제한 - 비밀번호 틀림 → 틀린 횟수 +1 (5번이면 10분 잠금, AdminUser 안에서 처리) - 로그인 성공 → 틀린
 * 기록 초기화 이미 잠긴 상태에서 시도한 건 "잠김" 이벤트라 횟수에 안 들어감
 */
@Component
public class LoginAttemptListener {

	private final AdminUserRepository adminUserRepository;

	public LoginAttemptListener(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@EventListener
	@Transactional
	public void onBadPassword(AuthenticationFailureBadCredentialsEvent event) {
		String username = event.getAuthentication().getName();
		adminUserRepository.findByUsername(username).ifPresent(AdminUser::recordLoginFailure);
	}

	@EventListener
	@Transactional
	public void onSuccess(AuthenticationSuccessEvent event) {
		String username = event.getAuthentication().getName();
		adminUserRepository.findByUsername(username).ifPresent(AdminUser::resetLoginFailure);
	}
}