package com.englandcamping.config;

import java.nio.file.Paths;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 주소 /uploads/파일이름 → 사진 저장 폴더의 그 파일 (파일 이름이 매번 무작위라 브라우저가 30일 동안 저장해 둬도 안전)
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

	private final String uploadDir;

	public WebConfig(@Value("${app.upload-dir}") String uploadDir) {
		this.uploadDir = uploadDir;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		// 예) C:\Users\kms\england-camping-uploads →
		// file:///C:/Users/kms/england-camping-uploads/
		String location = Paths.get(uploadDir).toAbsolutePath().normalize().toUri().toString();
		if (!location.endsWith("/")) {
			location = location + "/";
		}
		registry.addResourceHandler("/uploads/**").addResourceLocations(location)
				.setCacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic());
	}
}