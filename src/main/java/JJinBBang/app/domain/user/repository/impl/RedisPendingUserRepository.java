package JJinBBang.app.domain.user.repository.impl;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import JJinBBang.app.domain.user.entity.PendingUser;
import JJinBBang.app.domain.user.repository.PendingUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Repository
@ConditionalOnProperty(
	prefix = "app.repository",
	name = "mode",
	havingValue = "redis"
)
@RequiredArgsConstructor
public class RedisPendingUserRepository implements PendingUserRepository {

	private final RedisTemplate<String, Object> redisTemplate;

	private static final String PREFIX = "pending:user:";

	private String key(String pendingId) {
		return PREFIX + pendingId;
	}

	@Override
	public void save(PendingUser pendingUser) {
		String key = key(pendingUser.pendingId());
		Duration ttl = Duration.between(Instant.now(), pendingUser.expiresAt());
		if (ttl.isNegative() || ttl.isZero()) {
			return;
		}
		redisTemplate.opsForValue().set(key, pendingUser, ttl.getSeconds(), TimeUnit.SECONDS);
	}

	@Override
	public Optional<PendingUser> findById(String pendingId) {
		String key = key(pendingId);
		PendingUser user = (PendingUser) redisTemplate.opsForValue().get(key);
		return Optional.ofNullable(user);
	}

	@Override
	public void delete(String pendingId) {
		redisTemplate.delete(key(pendingId));
	}
}
