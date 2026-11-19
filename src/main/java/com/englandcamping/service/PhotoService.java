package com.englandcamping.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.englandcamping.domain.Photo;
import com.englandcamping.domain.PhotoCategory;
import com.englandcamping.domain.PhotoSlot;
import com.englandcamping.domain.PhotoSlotAssignment;
import com.englandcamping.repository.PhotoRepository;
import com.englandcamping.repository.PhotoSlotAssignmentRepository;
import com.englandcamping.storage.PhotoImageProcessor;
import com.englandcamping.storage.PhotoImageProcessor.ProcessedImage;
import com.englandcamping.storage.PhotoStorage;

/**
 * 사진: 올리기 / 삭제 / 분류·설명 / 순서 / 자리 지정 / 조회 실패하면 IllegalArgumentException (메시지를 화면
 * 안내 띠에 그대로 보여 줄 예정)
 */
@Service
@Transactional(readOnly = true)
public class PhotoService {

	/** 받는 파일 확장자 */
	private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png");
	/** 설명 최대 글자 수 */
	public static final int CAPTION_MAX_LENGTH = 100;

	private final PhotoRepository photoRepository;
	private final PhotoSlotAssignmentRepository slotRepository;
	private final PhotoStorage photoStorage;
	private final PhotoImageProcessor imageProcessor;

	public PhotoService(PhotoRepository photoRepository, PhotoSlotAssignmentRepository slotRepository,
			PhotoStorage photoStorage, PhotoImageProcessor imageProcessor) {
		this.photoRepository = photoRepository;
		this.slotRepository = slotRepository;
		this.photoStorage = photoStorage;
		this.imageProcessor = imageProcessor;
	}

	// ===== 조회 =====

	/** 전체 (갤러리 순서) */
	public List<Photo> findAll() {
		return photoRepository.findAllByOrderBySortOrderAscIdAsc();
	}

	/** 분류별 (null 이면 전체) */
	public List<Photo> findByCategory(PhotoCategory category) {
		return (category == null) ? findAll() : photoRepository.findByCategoryOrderBySortOrderAscIdAsc(category);
	}

	/** 메인 갤러리 칸용 앞 5장 */
	public List<Photo> findPreview() {
		return photoRepository.findTop5ByOrderBySortOrderAscIdAsc();
	}

	/** 하나 (없으면 404) */
	public Photo get(Long id) {
		return photoRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다."));
	}

	/** 자리별 사진 → 화면에서 ${slotPhotos['HERO']} 처럼 꺼내 씀 (빈 자리는 없음) */
	public Map<String, Photo> findSlotPhotos() {
		Map<String, Photo> map = new HashMap<>();
		for (PhotoSlotAssignment assignment : slotRepository.findAllWithPhotoBy()) {
			if (assignment.getPhoto() != null) {
				map.put(assignment.getSlot().name(), assignment.getPhoto());
			}
		}
		return map;
	}

	// ===== 올리기 =====

	/**
	 * 여러 장 한 번에 올리기 → 올린 장 수 중간에 한 장이라도 실패하면 이번에 저장한 파일을 지우고 DB도 되돌림 (전부 아니면 전무)
	 */
	@Transactional
	public int upload(List<MultipartFile> files, PhotoCategory category) {
		List<MultipartFile> realFiles = (files == null) ? List.of()
				: files.stream().filter(f -> f != null && !f.isEmpty()).toList();
		if (realFiles.isEmpty()) {
			throw new IllegalArgumentException("올릴 사진을 골라 주세요.");
		}
		if (category == null) {
			throw new IllegalArgumentException("분류를 골라 주세요.");
		}

		int order = photoRepository.findMaxSortOrder();
		List<String> savedFiles = new ArrayList<>();
		try {
			for (MultipartFile file : realFiles) {
				String originalName = file.getOriginalFilename();
				checkExtension(originalName);

				ProcessedImage image;
				try {
					image = imageProcessor.process(file.getBytes());
				} catch (IOException e) {
					throw new IllegalArgumentException("'" + originalName + "' 파일은 사진으로 읽을 수 없습니다. (JPG, PNG만 가능)");
				}

				// 저장 이름은 무작위 (원래 파일 이름은 쓰지 않음)
				String base = UUID.randomUUID().toString();
				String fileName = base + ".jpg";
				String thumbName = base + "_thumb.jpg";
				try {
					photoStorage.save(fileName, image.main());
					savedFiles.add(fileName);
					photoStorage.save(thumbName, image.thumb());
					savedFiles.add(thumbName);
				} catch (IOException e) {
					throw new IllegalStateException("사진 파일을 저장하지 못했습니다.", e);
				}

				photoRepository.save(new Photo(fileName, thumbName, category, image.width(), image.height(), ++order));
			}
		} catch (RuntimeException e) {
			savedFiles.forEach(photoStorage::delete); // 이번에 저장한 파일 정리
			throw e; // → DB도 자동으로 되돌려짐
		}
		return realFiles.size();
	}

	private void checkExtension(String fileName) {
		String ext = "";
		if (fileName != null && fileName.lastIndexOf('.') >= 0) {
			ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
		}
		if (!ALLOWED_EXTENSIONS.contains(ext)) {
			throw new IllegalArgumentException("'" + fileName + "' 은(는) 올릴 수 없는 형식입니다. (JPG, PNG만 가능)");
		}
	}

	// ===== 수정 / 순서 / 삭제 =====

	/** 분류, 설명 바꾸기 (설명은 비우면 없음) */
	@Transactional
	public void updateInfo(Long id, PhotoCategory category, String caption) {
		if (category == null) {
			throw new IllegalArgumentException("분류를 골라 주세요.");
		}
		String trimmed = (caption == null) ? "" : caption.strip();
		if (trimmed.length() > CAPTION_MAX_LENGTH) {
			throw new IllegalArgumentException("설명은 " + CAPTION_MAX_LENGTH + "자 이내로 입력해 주세요.");
		}
		get(id).updateInfo(category, trimmed.isEmpty() ? null : trimmed);
	}

	/**
	 * 한 칸 앞으로(up=true) / 뒤로(up=false) 순서를 1, 2, 3... 으로 다시 매기면서 바꿔서, 번호가 꼬여 있어도 안전
	 */
	@Transactional
	public void move(Long id, boolean up) {
		List<Photo> all = findAll();
		int index = -1;
		for (int i = 0; i < all.size(); i++) {
			if (all.get(i).getId().equals(id)) {
				index = i;
				break;
			}
		}
		if (index < 0) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "사진을 찾을 수 없습니다.");
		}
		int target = up ? index - 1 : index + 1;
		if (target < 0 || target >= all.size()) {
			return; // 이미 맨 앞/맨 뒤
		}
		Collections.swap(all, index, target);
		for (int i = 0; i < all.size(); i++) {
			all.get(i).changeSortOrder(i + 1);
		}
	}

	/**
	 * 삭제: 자리에서 빼고 → DB에서 지우고 → DB 저장이 끝난 "다음에" 파일 삭제 (DB가 실패했는데 파일만 사라지는 일이 없게)
	 */
	@Transactional
	public void delete(Long id) {
		Photo photo = get(id);
		String fileName = photo.getFileName();
		String thumbName = photo.getThumbName();

		slotRepository.deleteByPhoto(photo);
		photoRepository.delete(photo);

		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
			@Override
			public void afterCommit() {
				photoStorage.delete(fileName);
				photoStorage.delete(thumbName);
			}
		});
	}

	// ===== 자리 지정 =====

	/** 자리에 사진 넣기 (photoId 가 null 이면 자리 비우기) */
	@Transactional
	public void assignSlot(PhotoSlot slot, Long photoId) {
		Photo photo = (photoId == null) ? null : get(photoId);
		slotRepository.findById(slot).ifPresentOrElse(assignment -> assignment.changePhoto(photo),
				() -> slotRepository.save(new PhotoSlotAssignment(slot, photo)));
	}
}