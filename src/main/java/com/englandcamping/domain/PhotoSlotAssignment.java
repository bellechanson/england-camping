package com.englandcamping.domain;

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

	/** 자리 이름 (HERO 등) — 이게 기본키라 자리마다 한 줄만 생김 */
	@Id
	@Enumerated(EnumType.STRING)
	@Column(name = "slot", length = 30)
	private PhotoSlot slot;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "photo_id")
	private Photo photo;

	/** JPA가 쓰는 기본 생성자 (직접 쓰지 않음) */
	protected PhotoSlotAssignment() {
	}

	public PhotoSlotAssignment(PhotoSlot slot, Photo photo) {
		this.slot = slot;
		this.photo = photo;
	}

	/** 다른 사진으로 바꾸기 (null 이면 비우기) */
	public void changePhoto(Photo photo) {
		this.photo = photo;
	}

	public PhotoSlot getSlot() {
		return slot;
	}

	public Photo getPhoto() {
		return photo;
	}
}