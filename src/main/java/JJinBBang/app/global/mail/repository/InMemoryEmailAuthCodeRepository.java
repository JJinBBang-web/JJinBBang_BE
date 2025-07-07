package JJinBBang.app.global.mail.repository;

import JJinBBang.app.global.mail.dto.EmailAuthInfo;
import JJinBBang.app.global.mail.properties.MailAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository("inMemoryEmailAuthCodeRepository")
public class InMemoryEmailAuthCodeRepository implements EmailAuthCodeRepository {

    private final long expirationTime;
    private final Map<Long, EmailAuthInfo> emailAuthCodeMap = new ConcurrentHashMap<>();

    public InMemoryEmailAuthCodeRepository(MailAuthProperties props) {
        this.expirationTime = props.getExpirationTime() * 60_000;
    }

    @Override
    public void save(Long userId, String email, String authCode) {
        EmailAuthInfo info = new EmailAuthInfo(email, authCode, System.currentTimeMillis());
        EmailAuthInfo prev = emailAuthCodeMap.putIfAbsent(userId, info);
        if (prev != null) {
            throw new DuplicateKeyException("Duplicate key: " + userId);
        }
    }

    @Override
    public boolean isExistByUserId(Long userId) {
        EmailAuthInfo info = emailAuthCodeMap.get(userId);
        return info != null && !isExpired(info.timestamp());
    }

    @Override
    public Optional<EmailAuthInfo> findEmailAndAuthCodeByUserId(Long userId) {
        EmailAuthInfo info = emailAuthCodeMap.get(userId);
        if (info == null) {
            return Optional.empty();
        }
        if (isExpired(info.timestamp())) {
            emailAuthCodeMap.remove(userId);
            return Optional.empty();
        }
        return Optional.of(info);
    }

    @Override
    public void deleteByUserId(Long userId) {
        emailAuthCodeMap.remove(userId);
    }

    private boolean isExpired(long timestamp) {
        return System.currentTimeMillis() - timestamp >= expirationTime;
    }

    // 주기적으로 만료된 인증 코드 정리 (예: 1분마다)
    @Scheduled(fixedDelay = 60 * 1000) // 1분 간격
    public void cleanUpExpiredCodes() {
        emailAuthCodeMap.entrySet().removeIf(entry ->
                isExpired(entry.getValue().timestamp())
        );
    }
}
