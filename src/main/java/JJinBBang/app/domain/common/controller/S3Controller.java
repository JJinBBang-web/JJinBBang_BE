package JJinBBang.app.domain.common.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import JJinBBang.app.domain.common.dto.PresignedUrlResponse;
import JJinBBang.app.domain.common.service.S3Service;
import JJinBBang.app.global.template.ResTemplate;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/s3")
public class S3Controller {

	private final S3Service s3Service;

	/**
	 *  업로드용 Presigned URL 발급
	 * 프론트에서 파일 업로드 전에 요청
	 */
	@GetMapping("/presigned-upload")
	public ResTemplate<PresignedUrlResponse> getPresignedUploadUrl(
		@RequestParam String folder,
		@RequestParam String fileName
	) {
		return new ResTemplate<>(
			HttpStatus.OK, "처리 완료", s3Service.generateUploadPresignedUrl(folder, fileName)
		);
	}
}
