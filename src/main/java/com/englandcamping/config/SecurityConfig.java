package com.englandcamping.config;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.DefaultAuthenticationEventPublisher;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.englandcamping.service.AdminUserDetailsService;

/**
 * 로그인 설정 - /admin/** 만 로그인 필요, 나머지(손님 화면, CSS/JS)는 누구나 - 로그인 화면: /admin/login
 * (화면은 다음 차례에 만듦) - 로그인 유지(remember-me): 2주 - 로그아웃: /admin/logout (POST) → 메인으로
 */
@Configuration
public class SecurityConfig {

	/** 로그인 유지 기간 (초) = 14일 */
	private static final int REMEMBER_ME_SECONDS = 14 * 24 * 60 * 60;

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http, AdminUserDetailsService adminUserDetailsService,
			@Value("${admin.remember-me-key:}") String rememberMeKey) throws Exception {

		// 환경 변수가 없으면 임시 키 (재시작하면 자동 로그인이 풀리는 것뿐, 동작은 함)
		String key = (rememberMeKey == null || rememberMeKey.isBlank()) ? UUID.randomUUID().toString() : rememberMeKey;

		http.authorizeHttpRequests(auth -> auth
				// 로그인 화면은 누구나 (?error, ?locked 가 붙어도 허용 — 주소 경로만 보고 판단)
				.requestMatchers("/admin/login").permitAll().requestMatchers("/admin/**").authenticated() // 관리자 화면
				.anyRequest().permitAll()) // 그 외 전부 공개

				// 챗봇 창구는 CSRF 토큰 없이 받음 (로그인과 상관없고 아무것도 바꾸지 않는 질문 창구. 남용은 하루 횟수 제한으로 막음)
				.csrf(csrf -> csrf.ignoringRequestMatchers("/api/chat"))

				.formLogin(form -> form.loginPage("/admin/login") // 로그인 화면 주소 (GET)
						.loginProcessingUrl("/admin/login") // 로그인 처리 주소 (POST)
						.defaultSuccessUrl("/admin", true) // 성공하면 관리자 첫 화면
						// 실패 이유를 나눠서 화면에 보여 주려고: 잠김 / 그 외(아이디·비밀번호 틀림)
						.failureHandler((request, response, exception) -> {
							String reason = (exception instanceof LockedException) ? "locked" : "error";
							response.sendRedirect(request.getContextPath() + "/admin/login?" + reason);
						}).permitAll())

				.rememberMe(remember -> remember.key(key).rememberMeParameter("remember-me") // 로그인 화면 체크박스 name
						.tokenValiditySeconds(REMEMBER_ME_SECONDS).userDetailsService(adminUserDetailsService))

				.logout(logout -> logout.logoutUrl("/admin/logout").logoutSuccessUrl("/"));

		// CSRF 보호는 기본으로 켜져 있음 (th:action 폼은 토큰이 자동으로 들어감)
		return http.build();
	}

	/** 비밀번호 암호화 방식 (기본 bcrypt, 저장 값 앞에 {bcrypt}가 붙음) */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return PasswordEncoderFactories.createDelegatingPasswordEncoder();
	}

	/** 로그인 성공/실패 "이벤트"를 발행 → LoginAttemptListener가 받아서 틀린 횟수 기록 */
	@Bean
	public AuthenticationEventPublisher authenticationEventPublisher(ApplicationEventPublisher publisher) {
		return new DefaultAuthenticationEventPublisher(publisher);
	}
}