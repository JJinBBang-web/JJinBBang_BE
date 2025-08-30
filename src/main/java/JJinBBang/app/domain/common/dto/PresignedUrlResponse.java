package JJinBBang.app.domain.common.dto;

public record PresignedUrlResponse(
	String presignedUrl,
	String expiresAt,
	String cdnUrl
) {

	public static PresignedUrlResponse of (String presignedUrl, String expiresAt, String cdnUrl) {
		return new PresignedUrlResponse(presignedUrl, expiresAt, cdnUrl);
	}
}
