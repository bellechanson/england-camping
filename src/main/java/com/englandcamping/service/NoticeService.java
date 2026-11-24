package com.englandcamping.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.englandcamping.domain.Notice;
import com.englandcamping.repository.NoticeRepository;

/** 공지사항 (손님: 목록·상세 / 관리자: 쓰기·수정·삭제) */
@Service
@Transactional(readOnly = true)
public class NoticeService {

	/** 한 페이지에 보여 줄 공지 수 */
	public static final int PAGE_SIZE = 10;

	private final NoticeRepository noticeRepository;

	public NoticeService(NoticeRepository noticeRepository) {
		this.noticeRepository = noticeRepository;
	}

	/** 목록 (page는 0부터, 음수가 들어오면 0으로) */
	public Page<Notice> findPage(int page) {
		return noticeRepository
				.findAllByOrderByPinnedDescCreatedAtDescIdDesc(PageRequest.of(Math.max(page, 0), PAGE_SIZE));
	}

	/** 메인용 최근 3개 (고정 공지 먼저) */
	public List<Notice> findRecent() {
		return noticeRepository.findTop3ByOrderByPinnedDescCreatedAtDescIdDesc();
	}

	/** NEW 표시 기간: 쓴 날 포함 3일 (예: 24일에 쓴 글 → 24·25·26일 동안 NEW) */
	private static final int NEW_DAYS = 3;

	/** 이 시각 이후에 쓴 글이면 NEW (오늘 0시에서 2일 전) — 공지 목록·메인 최근 공지에서 사용 */
	public LocalDateTime newSince() {
		return LocalDate.now().minusDays(NEW_DAYS - 1).atStartOfDay();
	}

	/** 공지 전체 개수 (관리자 첫 화면) */
	public long countAll() {
		return noticeRepository.count();
	}

	/** 하나 (없으면 404 페이지) */
	public Notice get(Long id) {
		return noticeRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "공지를 찾을 수 없습니다."));
	}

	/** 이전 글 (없으면 null) — 상세 화면 아래 "이전 글" */
	public Notice findPrevious(Long id) {
		return noticeRepository.findFirstByIdLessThanOrderByIdDesc(id).orElse(null);
	}

	/** 다음 글 (없으면 null) — 상세 화면 아래 "다음 글" */
	public Notice findNext(Long id) {
		return noticeRepository.findFirstByIdGreaterThanOrderByIdAsc(id).orElse(null);
	}

	@Transactional
	public Notice create(String title, String content, boolean pinned) {
		return noticeRepository.save(new Notice(title.strip(), content.strip(), pinned));
	}

	@Transactional
	public void update(Long id, String title, String content, boolean pinned) {
		get(id).update(title.strip(), content.strip(), pinned);
	}

	@Transactional
	public void delete(Long id) {
		noticeRepository.delete(get(id));
	}
}