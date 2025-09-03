package JJinBBang.app.domain.common.service;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import JJinBBang.app.domain.common.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

	private final S3Presigner s3Presigner;
	private final S3Client s3;

	@Value("${cloud.aws.cdn.domain}")
	private String cdnDomain;

	@Value("${cloud.aws.s3.bucket}")
	private String bucket;

	@Value("${cloud.aws.s3.presign.ttl-minutes:5}")
	private long presignTtlMinutes;

	private static final Set<String> ALLOWED_FOLDERS = Set.of(
		"review", "profile"
	);

	private static final Map<String, String> EXT_TO_MIME = Map.ofEntries(
		Map.entry("jpg", "image/jpeg"),
		Map.entry("jpeg", "image/jpeg"),
		Map.entry("png", "image/png"),
		Map.entry("webp", "image/webp"),
		Map.entry("gif", "image/gif"),
		Map.entry("avif", "image/avif"),
		Map.entry("heic", "image/heic"),
		Map.entry("heif", "image/heif")
	);

	private static String extOf(String name) {
		if (name == null)
			return null;
		int dot = name.lastIndexOf('.');
		if (dot < 0 || dot == name.length() - 1)
			return null;
		return name.substring(dot + 1).toLowerCase();
	}

	//  S3에 이미지 업로드용 Presigned URL 발급 (PUT 방식)
	public PresignedUrlResponse generateUploadPresignedUrl(String folder, String originalFileName) {
		// 폴더명 체크
		String safeFolder = validateFolder(folder);     // 화이트리스트 체크
		// 파일명 정제: 특수문자를 언더스코어(_)로 변경, 긴 이름 자르기
		String safeName = sanitizeFileName(originalFileName); // 파일명 정리
		// 고유 키 생성: {폴더명}/{UUID}-{파일명}
		String key = joinKey(safeFolder, UUID.randomUUID() + "-" + safeName);
		// 파일 확장자 → MIME 타입
		String mime = fileNameToMime(safeName);

		// S3에 업로드할 파일 요청 정보
		PutObjectRequest objectRequest = PutObjectRequest.builder()
			.bucket(bucket)
			.key(key)
			.contentType(mime)
			.build();

		Duration ttl = Duration.ofMinutes(presignTtlMinutes);
		PresignedPutObjectRequest presigned = s3Presigner.presignPutObject(b -> b
			.signatureDuration(ttl)
			.putObjectRequest(objectRequest));

		Instant expiresAt = Instant.now().plus(ttl);
		String cdnUrl = buildCdnUrl(key);

		return PresignedUrlResponse.of(
			presigned.url().toString(),
			expiresAt.toString(),
			cdnUrl);
	}

	private static String sanitizeFileName(String original) {
		if (original == null || original.isBlank())
			throw new IllegalArgumentException("파일명이 비었습니다.");
		// 경로 분리자/제어문자 제거
		String cleaned = original.replaceAll("[\\\\/\\p{Cntrl}]", "_");
		// 허용 문자만
		cleaned = cleaned.replaceAll("[^A-Za-z0-9._-]", "_");
		// 너무 긴 이름 컷 (S3 키 전체 1024byte 제한 고려)
		return cleaned.length() > 200 ? cleaned.substring(0, 200) : cleaned;
	}

	private String fileNameToMime(String safeName) {
		String ext = extOf(safeName);
		String mime = ext != null ? EXT_TO_MIME.get(ext) : null;
		if (mime == null) {
			throw new IllegalArgumentException("허용되지 않은 파일 형식: " + safeName);
		}
		return mime;
	}

	private String validateFolder(String folder) {
		if (!ALLOWED_FOLDERS.contains(folder)) {
			throw new IllegalArgumentException("허용되지 않은 업로드 경로: " + folder);
		}
		return folder;
	}

	// 키 조합
	private String joinKey(String... parts) {
		return String.join("/", Arrays.stream(parts)
			.filter(Objects::nonNull)
			.map(p -> p.replaceAll("^/+", "").replaceAll("/+$", ""))
			.toList());
	}

	// CdnUrl에서 "폴더명/파일명" 추출
	private String buildCdnUrl(String key) {
		String host = cdnDomain.replaceAll("^https?://", "").replaceAll("/+$", "");
		String path = key.startsWith("/") ? key : "/" + key;
		return "https://" + host + path;
	}

	// S3에서 이미지 삭제
	public void deleteFile(String imageUrl) {
		String key = toKey(imageUrl);
		if (key == null || key.isBlank()) return;

		try {
			s3.deleteObject(DeleteObjectRequest.builder()
				.bucket(bucket)
				.key(key)
				.build());
		} catch (NoSuchKeyException e) {
			// 이미 없는 키면 무시
		} catch (S3Exception e) {
			log.warn("S3 delete failed. key={}, code={}", key, e.awsErrorDetails().errorCode(), e);
		}

	}

	// S3 URL에서 Key 추출
	private String toKey(String url) {
		if (url == null || url.isBlank()) {
			throw new IllegalArgumentException("빈 URL입니다.");
		}
		int schemeEnd = url.indexOf("://");
		int pathStart = url.indexOf('/', (schemeEnd >= 0 ? schemeEnd + 3 : 0));
		if (pathStart < 0 || pathStart == url.length() - 1) {
			throw new IllegalArgumentException("경로가 없는 URL입니다: " + url);
		}
		int q = url.indexOf('?', pathStart);
		String path = (q >= 0) ? url.substring(pathStart + 1, q) : url.substring(pathStart + 1);
		return URLDecoder.decode(path, StandardCharsets.UTF_8);
	}
}
