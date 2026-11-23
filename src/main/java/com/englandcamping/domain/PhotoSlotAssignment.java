package com.englandcamping.domain;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * "어느 자리에 어느 사진" 한 줄 (자리 하나당 한 행) photo 가 null 이면 비어 있는 자리 → 화면에서 그라데이션
 */
@Entity
@Table(name = "photo_slot")
public class PhotoSlotAssignment {

	/** 폰 화면 위치 기본값: 가운데 */
	public static final int DEFAULT_FOCUS_X = 50;

	/** 자리 이름 (HERO 등) — 이게 기본키라 자리마다 한 줄만 생김 */
	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "slot", length = 30)
	private PhotoSlot slot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "photo_id")
	private Photo photo;

	/**
	 * 폰에서 보일 가로 위치 (0 = 맨 왼쪽, 50 = 가운데, 100 = 맨 오른쪽) CSS object-position 의 가로 % 값과
	 * 같음. 지정 안 했으면 null → getFocusX()에서 50
	 */
	@Column(name = "focus_x")
	private Integer focusX;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected PhotoSlotAssignment() {
	}

	public PhotoSlotAssignment(PhotoSlot slot, Photo photo) {
		this.slot = slot;
		this.photo = photo;
	}

	/** 다른 사진으로 바꾸기 (null 이면 비우기). 사진이 바뀌면 위치는 다시 가운데로 */
	public void changePhoto(Photo photo) {
		Long oldId = (this.photo == null) ? null : this.photo.getId();
		Long newId = (photo == null) ? null : photo.getId();
		if (!Objects.equals(oldId, newId)) {
			this.focusX = null;
		}
		this.photo = photo;
	}

	/** 폰에서 보일 가로 위치 바꾸기 (0~100, 검사는 서비스에서) */
	public void changeFocusX(int focusX) {
		this.focusX = focusX;
	}

	public PhotoSlot getSlot() {
		return slot;
	}

	public Photo getPhoto() {
		return photo;
	}

	/** 지정 안 했으면 가운데(50) */
	public int getFocusX() {
		return (focusX == null) ? DEFAULT_FOCUS_X : focusX;
	}
}