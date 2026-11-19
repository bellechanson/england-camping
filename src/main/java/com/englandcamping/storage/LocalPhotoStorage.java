package com.englandcamping.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 사진 파일을 서버(내 PC) 폴더에 저장 폴더 위치: application.properties 의 app.upload-dir (환경 변수
 * ENGLAND_CAMPING_UPLOAD_DIR)
 */
@Component
public class LocalPhotoStorage implements PhotoStorage {

	private static final Logger log = LoggerFactory.getLogger(LocalPhotoStorage.class);

	private final Path baseDir;

	public LocalPhotoStorage(@Value("${app.upload-dir}") String uploadDir) throws IOException {
		this.baseDir = Paths.get(uploadDir).toAbsolutePath().normalize();
		Files.createDirectories(baseDir); // 폴더가 없으면 만듦
		log.info("[LocalPhotoStorage] 사진 저장 폴더: {}", baseDir);
	}

	@Override
	public void save(String fileName, byte[] data) throws IOException {
		Files.write(resolve(fileName), data);
	}

	@Override
	public void delete(String fileName) {
		try {
			Files.deleteIfExists(resolve(fileName));
		} catch (IOException e) {
			// 파일 하나 못 지웠다고 화면 처리를 실패시키지는 않음 (기록만)
			log.warn("[LocalPhotoStorage] 파일 삭제 실패: {}", fileName, e);
		}
	}

	/** 파일 이름 → 실제 경로. "../" 같은 걸로 저장 폴더 밖을 가리키면 거부 */
	private Path resolve(String fileName) {
		Path target = baseDir.resolve(fileName).normalize();
		if (!target.startsWith(baseDir)) {
			throw new IllegalArgumentException("잘못된 파일 이름입니다.");
		}
		return target;
	}
}