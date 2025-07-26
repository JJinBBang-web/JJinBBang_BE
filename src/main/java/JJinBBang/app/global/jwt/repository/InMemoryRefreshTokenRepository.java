package JJinBBang.app.global.jwt.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@ConditionalOnProperty(
        prefix = "app.repository",
        name   = "mode",
        havingValue = "inmemory"
)
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    private final ConcurrentHashMap<Long, RefreshToken> refreshTokenStore = new ConcurrentHashMap<>();

    @Override
    public String save(Long userId, String refreshToken, long expirationMillis) {
        refreshTokenStore.put(userId, new RefreshToken(refreshToken, System.currentTimeMillis() + expirationMillis));
        return refreshToken;
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        if(!refreshTokenStore.containsKey(userId)) {
            return Optional.empty();
        }
        String refreshToken = refreshTokenStore.get(userId).token;
        return Optional.of(refreshToken);
    }

    @Override
    public void deleteByUserId(Long userId) {
        refreshTokenStore.remove(userId);
    }

    // 유사 Redis 구현
    @Scheduled(fixedRate = 10 * 60 * 1000) // 10분마다 만료된 항목 제거
    public void cleanExpiredTokens() {
        refreshTokenStore.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }

    private record RefreshToken(String token, long expirationMillis) {
        public boolean isExpired() {
            return System.currentTimeMillis() > expirationMillis;
        }
    }
}
