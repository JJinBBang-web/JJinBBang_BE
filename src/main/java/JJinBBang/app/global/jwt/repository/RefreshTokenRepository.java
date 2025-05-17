package JJinBBang.app.global.jwt.repository;

import java.util.Optional;

public interface RefreshTokenRepository {

    String save(Long userId, String refreshToken, long expirationMillis);

    Optional<String> findByUserId(Long userId);

    void deleteByUserId(Long userId);
}
