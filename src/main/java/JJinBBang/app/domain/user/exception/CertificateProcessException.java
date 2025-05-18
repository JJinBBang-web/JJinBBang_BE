package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.UnprocessableGroupException;

public class CertificateProcessException extends UnprocessableGroupException {
    public CertificateProcessException(String message) {super(message);}

    public static CertificateProcessException ProcessException() {
        return new CertificateProcessException("PDF 업로드 중 문제가 발생했습니다.");
    }
}
