package com.englandcamping.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.AdminUser;

/** admin_user 테이블 조회/저장 */
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

	/** 로그인 아이디로 찾기 */
	Optional<AdminUser> findByUsername(String username);
}