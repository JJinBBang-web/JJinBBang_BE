package JJinBBang.app.global.jwt.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@ConditionalOnProperty( // 조건부 빈 등록 (application.yml의 app.storage.mode 속성에 따라 redis 모드일 때만 활성화)
        prefix = "app.repository",
        name   = "mode",
        havingValue = "redis"
)
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private static final String KEY_PREFIX = "refreshToken:";
    private final StringRedisTemplate redisTemplate;

    private String makeKey(Long userId) {
        return KEY_PREFIX + userId;
    }

    /**
     * @param expirationMillis 남은 수명(밀리초) 그대로 TTL 로 설정
     */
    @Override
    public String save(Long userId, String refreshToken, long expirationMillis) {
        String key = makeKey(userId);
        // 저장 및 TTL 설정 (덮어쓰기)
        redisTemplate.opsForValue()
                .set(key, refreshToken, expirationMillis, TimeUnit.MILLISECONDS);
        return refreshToken;
    }

    @Override
    public Optional<String> findByUserId(Long userId) {
        String key = makeKey(userId);
        String token = redisTemplate.opsForValue().get(key);
        return Optional.ofNullable(token);
    }

    @Override
    public void deleteByUserId(Long userId) {
        redisTemplate.delete(makeKey(userId));
    }
}
