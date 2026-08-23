package JJinBBang.app.domain.common.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

	@Mock
	private S3Presigner s3Presigner;

	@Mock
	private S3Client s3;

	private S3Service s3Service;

	@BeforeEach
	void setUp() {
		s3Service = new S3Service(s3Presigner, s3);
		ReflectionTestUtils.setField(s3Service, "cdnDomain",
			"objectstorage.ap-chuncheon-1.oraclecloud.com/n/example/b/review-images/o");
		ReflectionTestUtils.setField(s3Service, "bucket", "review-images");
	}

	@Test
	void deleteFileStripsConfiguredCdnBasePathFromObjectKey() {
		s3Service.deleteFile(
			"https://objectstorage.ap-chuncheon-1.oraclecloud.com/n/example/b/review-images/o/review/image.jpg");

		ArgumentCaptor<DeleteObjectRequest> request = ArgumentCaptor.forClass(DeleteObjectRequest.class);
		verify(s3).deleteObject(request.capture());
		assertThat(request.getValue().key()).isEqualTo("review/image.jpg");
	}
}
