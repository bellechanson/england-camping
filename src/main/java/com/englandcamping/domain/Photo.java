package com.englandcamping.domain;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 사진 한 장 (DB에는 파일 "이름"과 정보만, 실제 파일은 저장 폴더에) - fileName : 큰 사진 (긴 변 최대 1920px) -
 * thumbName : 미리보기 (긴 변 최대 600px)
 */
@Entity
@Table(name = "photo")
public class Photo {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 60)
	private String fileName;

	@Column(nullable = false, length = 60)
	private String thumbName;

	/** 갤러리 분류 (DB에는 "SITE" 같은 글자로 저장) */
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private PhotoCategory category;

	/** 사진 설명 (크게 볼 때 아래에 표시, 없어도 됨) */
	@Column(length = 100)
	private String caption;

	/** 갤러리 순서 (작을수록 앞) */
	@Column(nullable = false)
	private int sortOrder;

	/** 큰 사진의 가로/세로 (px) */
	private int width;
	private int height;

	@Column(nullable = false)
	private LocalDateTime createdAt;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected Photo() {
	}

	public Photo(String fileName, String thumbName, PhotoCategory category, int width, int height, int sortOrder) {
		this.fileName = fileName;
		this.thumbName = thumbName;
		this.category = category;
		this.width = width;
		this.height = height;
		this.sortOrder = sortOrder;
		this.createdAt = LocalDateTime.now();
	}

	/** 분류, 설명 바꾸기 */
	public void updateInfo(PhotoCategory category, String caption) {
		this.category = category;
		this.caption = caption;
	}

	/** 순서 바꾸기 */
	public void changeSortOrder(int sortOrder) {
		this.sortOrder = sortOrder;
	}

	/** 화면에서 쓸 큰 사진 주소 (WebConfig가 /uploads/** 를 저장 폴더와 연결) */
	public String getUrl() {
		return "/uploads/" + fileName;
	}

	/** 화면에서 쓸 미리보기 주소 */
	public String getThumbUrl() {
		return "/uploads/" + thumbName;
	}

	public Long getId() {
		return id;
	}

	public String getFileName() {
		return fileName;
	}

	public String getThumbName() {
		return thumbName;
	}

	public PhotoCategory getCategory() {
		return category;
	}

	public String getCaption() {
		return caption;
	}

	public int getSortOrder() {
		return sortOrder;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}
}