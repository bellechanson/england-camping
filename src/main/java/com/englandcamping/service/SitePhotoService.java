package com.englandcamping.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.englandcamping.domain.CampSite;
import com.englandcamping.repository.CampSiteRepository;
import com.englandcamping.service.PhotoService.SavedImage;

/**
 * 사이트 사진 (사이트당 1장, 손님 배치도 팝업에 보임). 사진 파일 처리(검사·회전·줄이기·저장)는 갤러리와 같은
 * PhotoService.saveImage 를 씀. 사이트 사진은 갤러리(photo 테이블)에는 들어가지 않음
 */
@Service
@Transactional(readOnly = true)
public class SitePhotoService {

	private final CampSiteRepository campSiteRepository;
	private final PhotoService photoService;

	public SitePhotoService(CampSiteRepository campSiteRepository, PhotoService photoService) {
		this.campSiteRepository = campSiteRepository;
		this.photoService = photoService;
	}

	/** 사이트 전부 (보여 줄 순서대로) */
	public List<CampSite> findAll() {
		return campSiteRepository.findAllByOrderBySortOrderAscIdAsc();
	}

	/**
	 * 사진 올리기 (이미 있으면 바꾸기). 옛 사진 파일은 DB 저장이 끝난 다음에 지움. 저장 중 실패하면 이번에 만든 파일을 지움
	 *
	 * @return 바꾸기 전에 사진이 있었는지 (안내 문구용)
	 */
	@Transactional
	public boolean upload(Long siteId, MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new IllegalArgumentException("올릴 사진을 골라 주세요.");
		}
		CampSite site = get(siteId);
		boolean hadPhoto = site.hasPhoto();
		String oldFile = site.getPhotoFile();
		String oldThumb = site.getPhotoThumb();

		SavedImage saved = photoService.saveImage(file);
		try {
			site.changePhoto(saved.fileName(), saved.thumbName());
		} catch (RuntimeException e) {
			photoService.deleteFilesNow(saved.fileName(), saved.thumbName());
			throw e;
		}
		photoService.deleteFilesAfterCommit(oldFile, oldThumb);
		return hadPhoto;
	}

	/** 사진 지우기 (없으면 그냥 넘어감) */
	@Transactional
	public void deletePhoto(Long siteId) {
		CampSite site = get(siteId);
		if (!site.hasPhoto()) {
			return;
		}
		String oldFile = site.getPhotoFile();
		String oldThumb = site.getPhotoThumb();
		site.changePhoto(null, null);
		photoService.deleteFilesAfterCommit(oldFile, oldThumb);
	}

	/** 사이트 하나 (없으면 404) */
	public CampSite get(Long siteId) {
		return campSiteRepository.findById(siteId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "사이트를 찾을 수 없습니다."));
	}
}