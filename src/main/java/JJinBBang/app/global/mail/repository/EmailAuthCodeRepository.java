package JJinBBang.app.global.mail.repository;

import JJinBBang.app.global.mail.dto.EmailAuthInfo;

import java.util.Optional;

public interface EmailAuthCodeRepository {
    void save(Long userId, String email, String authCode);

    boolean isExistByUserId(Long userId);

    Optional<EmailAuthInfo> findEmailAndAuthCodeByUserId(Long userId);

    void deleteByUserId(Long userId);
}
