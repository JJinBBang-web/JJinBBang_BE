package JJinBBang.app.global.mail.repository;

import java.util.Optional;

public interface EmailAuthCodeRepository {
    void save(String email, String authCode);

    boolean isExistByEmail(String email);

    Optional<String> findAuthCodeByEmail(String email);

    void deleteByEmail(String email);
}
