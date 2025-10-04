package JJinBBang.app.global.jwt.repository.impl;

import JJinBBang.app.global.jwt.enums.RotationResult;
import JJinBBang.app.global.jwt.repository.RefreshTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@ConditionalOnProperty(
    prefix = "app.repository",
    name   = "mode",
    havingValue = "inmemory"
)
public class InMemoryRefreshTokenRepository implements RefreshTokenRepository {

    /** userId -> (sessionId -> RefreshEntry) */
    private final Map<Long, ConcurrentHashMap<String, RefreshEntry>> userSessionStore = new ConcurrentHashMap<>();

    /** blacklist: tokenId(jti) -> expiryEpochMillis */
    private final Map<String, Long> blacklistStore = new ConcurrentHashMap<>();

    @Override
    public void saveOrUpdate(Long userId, String sessionId, String refreshTokenId, Duration timeToLive) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationEpochMillis = currentTimeMillis + Math.max(0, timeToLive.toMillis());
        userSessionStore
            .computeIfAbsent(userId, id -> new ConcurrentHashMap<>())
            .put(sessionId, new RefreshEntry(refreshTokenId, expirationEpochMillis));
    }

    @Override
    public Optional<String> getRefreshTokenId(Long userId, String sessionId) {
        long currentTimeMillis = System.currentTimeMillis();
        Map<String, RefreshEntry> sessionMap = userSessionStore.get(userId);
        if (sessionMap == null) return Optional.empty();
        RefreshEntry refreshEntry = sessionMap.get(sessionId);
        if (refreshEntry == null) return Optional.empty();

        if (refreshEntry.expirationEpochMillis <= currentTimeMillis) {
            sessionMap.remove(sessionId);
            if (sessionMap.isEmpty()) userSessionStore.remove(userId);
            return Optional.empty();
        }
        return Optional.of(refreshEntry.refreshTokenId);
    }

    @Override
    public void blacklistRefreshTokenId(String tokenId, Duration timeToLive) {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationEpochMillis = currentTimeMillis + Math.max(0, timeToLive.toMillis());
        // 이미 블랙리스트에 있으면 더 긴 만료시간 유지
        blacklistStore.merge(tokenId, expirationEpochMillis, Math::max);
    }

    @Override
    public boolean isBlacklisted(String tokenId) {
        long currentTimeMillis = System.currentTimeMillis();
        Long expirationEpochMillis = blacklistStore.get(tokenId);
        if (expirationEpochMillis == null) return false;
        if (expirationEpochMillis <= currentTimeMillis) {
            blacklistStore.remove(tokenId);
            return false;
        }
        return true;
    }

    @Override
    public RotationResult tryRotate(Long userId,
        String sessionId, 					// 유저의 로그인 당 세션 ID
        String presentedRefreshTokenId, 	// 제출된(사용된) 리프레시 토큰 ID (jti)
        String newRefreshTokenId,			// 새로 발급된 리프레시 토큰 ID (jti)
        Duration presentedTokenTtl,			// 제출된 토큰의 남은 유효기간 (블랙리스트 보관용)
        Duration newTokenTtl,				// 새로 발급된 토큰의 유효기간
        boolean revokeAllOnReuse			// 재사용 감지 시 모든 세션 폐기 여부
    ) {
        long currentTimeMillis = System.currentTimeMillis();
        long newRefreshExpiry = currentTimeMillis + Math.max(0, newTokenTtl.toMillis());
        long presentedBlacklistExpiry = currentTimeMillis + Math.max(0, presentedTokenTtl.toMillis());

        ConcurrentHashMap<String, RefreshEntry> sessionMap =
            userSessionStore.computeIfAbsent(userId, id -> new ConcurrentHashMap<>());

        final RotationResult[] rotationResultBox = new RotationResult[1];

        // 세션 단위로 원자 처리
        sessionMap.compute(sessionId, (sid, currentEntry) -> {
            if (currentEntry == null || currentEntry.expirationEpochMillis <= currentTimeMillis) {
                // 현재 세션이 없거나 만료됨: 제출된 토큰은 재사용 방지 위해 블랙리스트
                blacklistStore.merge(presentedRefreshTokenId, presentedBlacklistExpiry, Math::max);
                // 세션이 없으면 : 로그인 하지 않음
                // 세션이 있는데 만료면 : 로그인 만료 -> 재로그인 유도
                rotationResultBox[0] = (currentEntry == null) ?
                    RotationResult.NOT_REGISTERED : RotationResult.MISMATCH_OR_EXPIRED;
                return null; // 제거
            }

            // 정상 케이스: current jti == presented jti → 교체 + presented 블랙리스트
            if (currentEntry.refreshTokenId.equals(presentedRefreshTokenId)) {
                blacklistStore.merge(presentedRefreshTokenId, presentedBlacklistExpiry, Math::max);
                rotationResultBox[0] = RotationResult.SUCCESS;
                return new RefreshEntry(newRefreshTokenId, newRefreshExpiry);
            }

            // 재사용/불일치: 제출된 jti 블랙리스트
            blacklistStore.merge(presentedRefreshTokenId, presentedBlacklistExpiry, Math::max);

            // 정책에 따라 세션 폐기 범위 결정
            if (revokeAllOnReuse) { // 모든 세션 폐기
                // 현재 세션도 블랙리스트 후 제거
                blacklistStore.merge(currentEntry.refreshTokenId, newRefreshExpiry, Math::max);
                // user 전체 세션 제거
                revokeAllSessions(userId, Duration.ofMillis(newRefreshExpiry - currentTimeMillis));
                rotationResultBox[0] = RotationResult.MISMATCH_OR_EXPIRED;
                return null;
            } else { // 해당 세션만 폐기
                blacklistStore.merge(currentEntry.refreshTokenId, newRefreshExpiry, Math::max);
                rotationResultBox[0] = RotationResult.MISMATCH_OR_EXPIRED;
                return null; // 제거하여 강제 재로그인 유도(해당 세션)
            }
        });

        if (sessionMap.isEmpty()) {
            userSessionStore.remove(userId, sessionMap);
        }
        return rotationResultBox[0];
    }

    @Override
    public void revokeSession(Long userId, String sessionId, Duration blacklistTtl) {
        long currentTimeMillis = System.currentTimeMillis();
        ConcurrentHashMap<String, RefreshEntry> sessionMap = userSessionStore.get(userId);
        if (sessionMap == null) return;
        sessionMap.computeIfPresent(sessionId, (sid, currentEntry) -> {
            long blacklistExpiry = currentTimeMillis + Math.max(0, blacklistTtl.toMillis());
            blacklistStore.merge(currentEntry.refreshTokenId, blacklistExpiry, Math::max);
            return null; // 세션 제거
        });
        if (sessionMap.isEmpty()) userSessionStore.remove(userId); // 아무 세션도 없으면 유저 키 제거
    }

    @Override
    public void revokeAllSessions(Long userId, Duration blacklistTtl) {
        long currentTimeMillis = System.currentTimeMillis();
        ConcurrentHashMap<String, RefreshEntry> sessionMap = userSessionStore.remove(userId);
        if (sessionMap == null) return;
        long blacklistExpiry = currentTimeMillis + Math.max(0, blacklistTtl.toMillis());
        sessionMap.forEach((sid, entry) ->
            blacklistStore.merge(entry.refreshTokenId, blacklistExpiry, Math::max)
        );
    }

    @Override
    public Set<String> listSessionIds(Long userId) {
        Map<String, RefreshEntry> sessionMap = userSessionStore.get(userId);
        if (sessionMap == null) return Collections.emptySet();
        return sessionMap.keySet();
    }

    @Scheduled(fixedRate = 60_000) // 1 minute
    public void evictExpiredEntries() {
        long currentTimeMillis = System.currentTimeMillis();

        userSessionStore.forEach((uid, sessionMap) -> {
            sessionMap.entrySet().removeIf(e -> e.getValue().expirationEpochMillis <= currentTimeMillis);
            if (sessionMap.isEmpty()) userSessionStore.remove(uid, sessionMap);
        });

        blacklistStore.entrySet().removeIf(e -> e.getValue() <= currentTimeMillis);
    }

    private static final class RefreshEntry {
        final String refreshTokenId;
        final long expirationEpochMillis;
        RefreshEntry(String refreshTokenId, long expirationEpochMillis) {
            this.refreshTokenId = refreshTokenId;
            this.expirationEpochMillis = expirationEpochMillis;
        }
    }
}
