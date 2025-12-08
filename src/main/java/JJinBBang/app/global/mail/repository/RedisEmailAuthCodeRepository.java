package JJinBBang.app.global.mail.repository;

import JJinBBang.app.global.mail.dto.EmailAuthInfo;
import JJinBBang.app.global.mail.properties.MailAuthProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
@ConditionalOnProperty( // 조건부 빈 등록 (application.yml의 app.storage.mode 속성에 따라 redis 모드일 때만 활성화)
		prefix = "app.repository", name = "mode", havingValue = "redis")
@RequiredArgsConstructor
public class RedisEmailAuthCodeRepository implements EmailAuthCodeRepository {

	private static final String KEY_PREFIX = "emailAuth:";

	private final RedisTemplate<String, EmailAuthInfo> redisTemplate;
	private final MailAuthProperties props;

	private String makeKey(Long userId) {
		return KEY_PREFIX + userId;
	}

	@Override
	public void save(Long userId, String email, String authCode) {
		String key = makeKey(userId);

		// 저장 및 TTL 설정 (덮어쓰기)
		EmailAuthInfo info = new EmailAuthInfo(email, authCode, System.currentTimeMillis());
		redisTemplate.opsForValue()
				.set(key, info, props.getExpirationTime(), TimeUnit.MILLISECONDS);
	}

	@Override
	public boolean isExistByUserId(Long userId) {
		String key = makeKey(userId);
		return Boolean.TRUE.equals(redisTemplate.hasKey(key));
		// TTL이 지나면 키 자동 삭제
	}

	@Override
	public Optional<EmailAuthInfo> findEmailAndAuthCodeByUserId(Long userId) {
		String key = makeKey(userId);
		EmailAuthInfo info = redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(info);
	}

	@Override
	public void deleteByUserId(Long userId) {
		String key = makeKey(userId);
		redisTemplate.delete(key);
	}
}