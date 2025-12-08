package JJinBBang.app.global.mail.repository;

import JJinBBang.app.global.mail.dto.EmailAuthInfo;
import JJinBBang.app.global.mail.properties.MailAuthProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Repository
@ConditionalOnProperty(prefix = "app.repository", name = "mode", havingValue = "inmemory")
public class InMemoryEmailAuthCodeRepository implements EmailAuthCodeRepository {

	/** 이메일 인증 코드 만료 시간 (밀리초) */
	private final long expirationTimeMillis;
	private final Map<Long, EmailAuthInfo> emailAuthCodeMap = new ConcurrentHashMap<>();

	public InMemoryEmailAuthCodeRepository(MailAuthProperties props) {
		this.expirationTimeMillis = props.getExpirationTime();
	}

	@Override
	public void save(Long userId, String email, String authCode) {
		long now = System.currentTimeMillis();
		emailAuthCodeMap.compute(userId, (k, prev) -> {
			// prev가 없거나, 만료됐거나, 혹은 정책상 재발급은 항상 갱신
			return new EmailAuthInfo(email, authCode, now);
		});
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
		return System.currentTimeMillis() - timestamp >= expirationTimeMillis;
	}

	// 주기적으로 만료된 인증 코드 정리 (예: 1분마다)
	@Scheduled(fixedDelay = 60 * 1000) // 1분 간격
	public void cleanUpExpiredCodes() {
		emailAuthCodeMap.entrySet().removeIf(entry -> isExpired(entry.getValue().timestamp()));
	}
}
