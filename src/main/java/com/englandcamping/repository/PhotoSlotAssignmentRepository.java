package com.englandcamping.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.englandcamping.domain.Photo;
import com.englandcamping.domain.PhotoSlot;
import com.englandcamping.domain.PhotoSlotAssignment;

/** photo_slot 테이블 조회/저장 (기본키 = 자리 이름) */
public interface PhotoSlotAssignmentRepository extends JpaRepository<PhotoSlotAssignment, PhotoSlot> {

	/** 자리 전체 + 사진 정보까지 한 번에 (자리마다 따로 조회하지 않게) */
	@EntityGraph(attributePaths = "photo")
	List<PhotoSlotAssignment> findAllWithPhotoBy();

	/** 사진을 지울 때, 그 사진이 들어 있던 자리도 같이 비움 */
	void deleteByPhoto(Photo photo);
}