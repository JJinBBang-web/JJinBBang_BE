package JJinBBang.app.domain.user.exception;

import JJinBBang.app.global.error.exception.InvalidGroupException;

public class CertificateBadRequestException extends InvalidGroupException {
    public CertificateBadRequestException(String message) {
        super(message);
    }

    public static CertificateBadRequestException FileUploadException() {
      return new CertificateBadRequestException("업로드할 파일을 확인해주세요.");
    }

    public static CertificateBadRequestException DriveAPIException() {
        return new CertificateBadRequestException("파일 업로드 요청이 잘못되었습니다.");
    }

    public static CertificateBadRequestException SheetsRequestException() {
        return new CertificateBadRequestException("Sheets API 요청이 잘못되었습니다.");
    }
}
