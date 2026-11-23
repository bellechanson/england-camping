package com.englandcamping.storage;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.springframework.stereotype.Component;

import net.coobird.thumbnailator.Thumbnails;

/**
 * 올린 사진 → 큰 사진 + 미리보기 (둘 다 JPG로 새로 만듦) 1) 폰 사진 회전 정보(EXIF)를 먼저 적용해서 똑바로 세움 2)
 * 크기 줄이기 (작은 사진은 키우지 않음) 3) 새로 그려서 저장 → GPS 위치, 촬영 기기 같은 정보는 따라오지 않음
 */
@Component
public class PhotoImageProcessor {

	/** 큰 사진: 긴 변 최대 */
	public static final int MAX_SIZE = 2560;
	/** 미리보기: 긴 변 최대 */
	public static final int THUMB_SIZE = 600;
	/** JPG 화질 (0~1) */
	private static final float QUALITY = 0.85f;

	/** 처리 결과 */
	public record ProcessedImage(byte[] main, byte[] thumb, int width, int height) {
	}

	/** 사진으로 읽을 수 없는 파일이면 IOException */
	public ProcessedImage process(byte[] original) throws IOException {
		// 원래 크기 그대로, 회전만 적용해서 읽기
		BufferedImage upright = Thumbnails.of(new ByteArrayInputStream(original)).scale(1.0).asBufferedImage();

		BufferedImage main = toRgb(shrink(upright, MAX_SIZE));
		BufferedImage thumb = toRgb(shrink(main, THUMB_SIZE));

		return new ProcessedImage(toJpg(main), toJpg(thumb), main.getWidth(), main.getHeight());
	}

	/** 긴 변이 maxSize 보다 크면 그 크기로 줄임 (비율 유지), 작으면 그대로 */
	private BufferedImage shrink(BufferedImage image, int maxSize) throws IOException {
		if (Math.max(image.getWidth(), image.getHeight()) <= maxSize) {
			return image;
		}
		return Thumbnails.of(image).size(maxSize, maxSize).asBufferedImage();
	}

	/** 투명한 부분(PNG)이 있으면 흰 바탕에 올려서 일반 RGB 사진으로 */
	private BufferedImage toRgb(BufferedImage image) {
		if (image.getType() == BufferedImage.TYPE_INT_RGB) {
			return image;
		}
		BufferedImage rgb = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
		Graphics2D g = rgb.createGraphics();
		g.setColor(Color.WHITE);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());
		g.drawImage(image, 0, 0, null);
		g.dispose();
		return rgb;
	}

	/** JPG 파일 내용(byte[])으로 */
	private byte[] toJpg(BufferedImage image) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		Thumbnails.of(image).scale(1.0).outputFormat("jpg").outputQuality(QUALITY).toOutputStream(out);
		return out.toByteArray();
	}
}