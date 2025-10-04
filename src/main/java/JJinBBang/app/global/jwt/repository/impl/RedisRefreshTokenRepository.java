package JJinBBang.app.global.jwt.repository.impl;

import JJinBBang.app.global.jwt.enums.RotationResult;
import JJinBBang.app.global.jwt.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Repository
@ConditionalOnProperty(
    prefix = "app.repository",
    name   = "mode",
    havingValue = "redis"
)
@RequiredArgsConstructor
public class RedisRefreshTokenRepository implements RefreshTokenRepository {

    private final StringRedisTemplate redis;

    private static String sessionCurrentRefreshTokenKey(long userId, String sessionId) {
        return "refresh-token:user:" + userId + ":session:" + sessionId;
    }

    private static String userSessionsSetKey(long userId) {
        return "refresh-token-sessions:user:" + userId;
    }

    private static String refreshTokenBlacklistKey(String tokenId) {
        return "refresh-token-blacklist:" + tokenId;
    }

    @Override
    public void saveOrUpdate(Long userId, String sessionId, String refreshTokenId, Duration timeToLive) {
        String sessionKey = sessionCurrentRefreshTokenKey(userId, sessionId);
        String sessionsKey = userSessionsSetKey(userId);

        redis.opsForValue().set(sessionKey, refreshTokenId, timeToLive);
        redis.opsForSet().add(sessionsKey, sessionId);
    }

    @Override
    public Optional<String> getRefreshTokenId(Long userId, String sessionId) {
        String value = redis.opsForValue().get(sessionCurrentRefreshTokenKey(userId, sessionId));
        if (value == null) {
            redis.opsForSet().remove(userSessionsSetKey(userId), sessionId);
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public void blacklistRefreshTokenId(String tokenId, Duration timeToLive) {
        redis.opsForValue().set(refreshTokenBlacklistKey(tokenId), "1", timeToLive);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        return Boolean.TRUE.equals(redis.hasKey(refreshTokenBlacklistKey(tokenId)));
    }

    /**
     * 경쟁 조건 방지를 위해 synchronized 블록 사용 (단일 인스턴스 기준)
     * 분산환경에서는 Redisson 분산락 등을 고려해야 함
     */
    @Override
    public synchronized RotationResult tryRotate(Long userId,
        String sessionId,
        String presentedRefreshTokenId,
        String newRefreshTokenId,
        Duration presentedTokenTtl,
        Duration newTokenTtl,
        boolean revokeAllOnReuse) {
        String sessionKey = sessionCurrentRefreshTokenKey(userId, sessionId);
        String sessionsKey = userSessionsSetKey(userId);

        String current = redis.opsForValue().get(sessionKey);
        if (current == null) {
            blacklistRefreshTokenId(presentedRefreshTokenId, presentedTokenTtl);
            redis.opsForSet().remove(sessionsKey, sessionId);
            return RotationResult.NOT_REGISTERED;
        }

        if (Objects.equals(current, presentedRefreshTokenId)) {
            // 정상 회전
            redis.opsForValue().set(sessionKey, newRefreshTokenId, newTokenTtl);
            blacklistRefreshTokenId(presentedRefreshTokenId, presentedTokenTtl);
            redis.opsForSet().add(sessionsKey, sessionId);
            return RotationResult.SUCCESS;
        } else {
            // mismatch
            blacklistRefreshTokenId(presentedRefreshTokenId, presentedTokenTtl);

            if (revokeAllOnReuse) {
                revokeAllSessions(userId, newTokenTtl);
            } else {
                revokeSession(userId, sessionId, newTokenTtl);
            }
            return RotationResult.MISMATCH_OR_EXPIRED;
        }
    }

    @Override
    public void revokeSession(Long userId, String sessionId, Duration blacklistTtl) {
        String sessionKey = sessionCurrentRefreshTokenKey(userId, sessionId);
        String current = redis.opsForValue().get(sessionKey);
        if (current != null) {
            blacklistRefreshTokenId(current, blacklistTtl);
        }
        redis.delete(sessionKey);
        redis.opsForSet().remove(userSessionsSetKey(userId), sessionId);
    }

    @Override
    public void revokeAllSessions(Long userId, Duration blacklistTtl) {
        String sessionsKey = userSessionsSetKey(userId);
        Set<String> sessionIds = redis.opsForSet().members(sessionsKey);
        if (sessionIds == null || sessionIds.isEmpty()) return;

        for (String sid : sessionIds) {
            String key = sessionCurrentRefreshTokenKey(userId, sid);
            String current = redis.opsForValue().get(key);
            if (current != null) {
                blacklistRefreshTokenId(current, blacklistTtl);
            }
            redis.delete(key);
            redis.opsForSet().remove(sessionsKey, sid);
        }
    }

    @Override
    public Set<String> listSessionIds(Long userId) {
        Set<String> ids = redis.opsForSet().members(userSessionsSetKey(userId));
        return (ids == null) ? Collections.emptySet() : ids;
    }
}
