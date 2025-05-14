package JJinBBang.app.global.mail.repository;

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
    private final Map<String, EmailAuthInfo> emailAuthCodeMap = new ConcurrentHashMap<>();

    public InMemoryEmailAuthCodeRepository(MailAuthProperties props) {
        this.expirationTime = props.getExpirationTime() * 60_000;
    }

    @Override
    public void save(String email, String authCode) {
        EmailAuthInfo info = new EmailAuthInfo(authCode, System.currentTimeMillis());
        EmailAuthInfo prev = emailAuthCodeMap.putIfAbsent(email, info);
        if (prev != null) {
            throw new DuplicateKeyException("Duplicate key: " + email);
        }
    }

    @Override
    public boolean isExistByEmail(String email) {
        EmailAuthInfo info = emailAuthCodeMap.get(email);
        return info != null && !isExpired(info.timestamp);
    }

    @Override
    public Optional<String> findAuthCodeByEmail(String email) {
        EmailAuthInfo info = emailAuthCodeMap.get(email);
        if (info == null) {
            return Optional.empty();
        }
        if (isExpired(info.timestamp)) {
            emailAuthCodeMap.remove(email);
            return Optional.empty();
        }
        return Optional.of(info.code);
    }

    @Override
    public void deleteByEmail(String email) {
        emailAuthCodeMap.remove(email);
    }

    private boolean isExpired(long timestamp) {
        return System.currentTimeMillis() - timestamp >= expirationTime;
    }

    // 주기적으로 만료된 인증 코드 정리 (예: 1분마다)
    @Scheduled(fixedDelay = 60 * 1000) // 1분 간격
    public void cleanUpExpiredCodes() {
        emailAuthCodeMap.entrySet().removeIf(entry ->
                isExpired(entry.getValue().timestamp)
        );
    }

    // 내부 저장용 클래스
    private record EmailAuthInfo(String code, long timestamp) { }
}
