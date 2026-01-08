package JJinBBang.app.global.jwt.repository;

import java.time.Duration;
import java.util.Optional;
import java.util.Set;

import JJinBBang.app.global.jwt.enums.RotationResult;

public interface RefreshTokenRepository {

    /** (userId, sessionId) 기준으로 현재 Refresh Refresh Token ID 저장/갱신 */
    void saveOrUpdate(Long userId, String sessionId, String refreshTokenId, Duration timeToLive);

    /** (userId, sessionId) 기준 현재 Refresh Token ID 조회 */
    Optional<String> getRefreshTokenId(Long userId, String sessionId);

    /** 제출된 jti, 현재 jti 등을 TTL 동안 블랙리스트에 등록 */
    void blacklistRefreshTokenId(String tokenId, Duration timeToLive);

    /** jti 가 블랙리스트에 포함되어 있는지 */
    boolean isBlacklisted(String tokenId);

    /**
     * 세션 단위 원자 회전(compare-and-set):
     * - 저장된 current jti == presented jti 일 때만 새 jti로 교체하고, presented jti 는 블랙리스트에 등록.
     * - 불일치/만료/미등록이면 presented jti 를 블랙리스트 처리하고 실패 코드 반환.
     * - revokeAllOnReuse=true 이면 재사용 탐지 시 유저의 모든 세션을 폐기. 아니면 해당 세션만 폐기.
     */
    RotationResult tryRotate(Long userId,
        String sessionId,
        String presentedRefreshTokenId,
        String newRefreshTokenId,
        Duration presentedTokenTtl,
        Duration newTokenTtl,
        boolean revokeAllOnReuse);

    /** 특정 세션만 폐기(블랙리스트 후 제거) */
    void revokeSession(Long userId, String sessionId, Duration blacklistTtl);

    /** 유저의 모든 세션 폐기(블랙리스트 후 제거) */
    void revokeAllSessions(Long userId, Duration blacklistTtl);

    /** 유저가 가진 활성 세션 ID 목록(운영/디버깅용) */
    Set<String> listSessionIds(Long userId);
}
