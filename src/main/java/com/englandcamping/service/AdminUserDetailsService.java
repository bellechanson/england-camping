package com.englandcamping.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.englandcamping.domain.AdminUser;
import com.englandcamping.repository.AdminUserRepository;

/**
 * 로그인할 때 Spring Security가 "이 아이디의 계정 정보"를 물어보는 곳. 잠겨 있으면 accountLocked(true) →
 * 비밀번호 확인 전에 LockedException
 */
@Service
@Transactional(readOnly = true)
public class AdminUserDetailsService implements UserDetailsService {

	private final AdminUserRepository adminUserRepository;

	public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
		this.adminUserRepository = adminUserRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		AdminUser admin = adminUserRepository.findByUsername(username)
				.orElseThrow(() -> new UsernameNotFoundException("관리자 계정을 찾을 수 없습니다."));

		return User.withUsername(admin.getUsername()).password(admin.getPassword()).roles("ADMIN")
				.accountLocked(admin.isLocked()).build();
	}
}