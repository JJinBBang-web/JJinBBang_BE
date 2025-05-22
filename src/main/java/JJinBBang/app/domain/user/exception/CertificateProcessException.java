package JJinBBang.app.domain.user.exception;

import JJinBBang.app.domain.user.service.CertificateService;
import JJinBBang.app.global.error.exception.UnprocessableGroupException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CertificateProcessException extends UnprocessableGroupException {
    public CertificateProcessException(String message) {super(message);}

    public static CertificateProcessException DriveProcessException() {
        log.error("[Drive IO 오류]");
        return new CertificateProcessException("파일 업로드 중 문제가 발생했습니다.");
    }

    public static CertificateProcessException SheetsProcessException() {
        return new CertificateProcessException("시트 삽입 중 문제가 발생했습니다.");
    }
}
