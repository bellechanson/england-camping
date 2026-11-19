package com.englandcamping.storage;

import java.io.IOException;

/**
 * 사진 "파일"을 어디에 둘지 담당하는 약속(인터페이스). 지금은 LocalPhotoStorage(서버 폴더)가 이 약속을 지키고, 나중에
 * S3 같은 클라우드로 바꾸면 이 약속을 지키는 클래스 하나만 새로 만들면 된다.
 */
public interface PhotoStorage {

	/** 파일 저장 (이름은 PhotoService가 정해서 넘김) */
	void save(String fileName, byte[] data) throws IOException;

	/** 파일 삭제 (없으면 그냥 넘어감) */
	void delete(String fileName);
}