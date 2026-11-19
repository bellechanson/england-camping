package com.englandcamping.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 관리자 계정 (지금은 아빠 계정 하나) - password 에는 암호화된 값만 저장 (예: {bcrypt}$2a$10$...) - 5번
 * 틀리면 10분 잠금
 */
@Entity
@Table(name = "admin_user")
public class AdminUser {

	/** 몇 번 틀리면 잠글지 */
	public static final int MAX_LOGIN_FAILURES = 5;
	/** 몇 분 잠글지 */
	public static final int LOCK_MINUTES = 10;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	/** 로그인 아이디 */
	@Column(nullable = false, unique = true, length = 30)
	private String username;

	/** 암호화된 비밀번호 (원래 비밀번호는 어디에도 저장하지 않음) */
	@Column(nullable = false, length = 100)
	private String password;

	/** 연속으로 틀린 횟수 */
	@Column(nullable = false)
	private int failedCount;

	/** 이 시간까지 잠김 (null이면 안 잠김) */
	private LocalDateTime lockedUntil;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	/** 마지막으로 비밀번호를 바꾼 시간 */
	private LocalDateTime passwordChangedAt;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected AdminUser() {
	}

	public AdminUser(String username, String encodedPassword) {
		this.username = username;
		this.password = encodedPassword;
		this.createdAt = LocalDateTime.now();
	}

	/** 지금 잠겨 있는지 */
	public boolean isLocked() {
		return lockedUntil != null && lockedUntil.isAfter(LocalDateTime.now());
	}

	/** 비밀번호 틀림 1회 기록. 정해진 횟수가 되면 잠그고 횟수는 다시 0부터 */
	public void recordLoginFailure() {
		failedCount++;
		if (failedCount >= MAX_LOGIN_FAILURES) {
			lockedUntil = LocalDateTime.now().plusMinutes(LOCK_MINUTES);
			failedCount = 0;
		}
	}

	/** 로그인 성공 → 틀린 기록 초기화 */
	public void resetLoginFailure() {
		failedCount = 0;
		lockedUntil = null;
	}

	/** 비밀번호 바꾸기 (암호화된 값을 받음) */
	public void changePassword(String encodedPassword) {
		this.password = encodedPassword;
		this.passwordChangedAt = LocalDateTime.now();
	}

	public Long getId() {
		return id;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public int getFailedCount() {
		return failedCount;
	}

	public LocalDateTime getLockedUntil() {
		return lockedUntil;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public LocalDateTime getPasswordChangedAt() {
		return passwordChangedAt;
	}
}