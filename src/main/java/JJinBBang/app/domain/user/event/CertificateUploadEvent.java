package JJinBBang.app.domain.user.event;

public record CertificateUploadEvent(
        Long userId,
        String fileLink, // Google Drive URL
        String fileName
) {
}
