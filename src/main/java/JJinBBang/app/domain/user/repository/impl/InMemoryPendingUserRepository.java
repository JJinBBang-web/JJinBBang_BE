package JJinBBang.app.domain.user.repository.impl;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
	havingValue = "inmemory"
)
@RequiredArgsConstructor
public class InMemoryPendingUserRepository implements PendingUserRepository {

	private final Map<String, PendingUser> store = new ConcurrentHashMap<>();

	@Override
	public void save(PendingUser pendingUser) {
		store.put(pendingUser.pendingId(), pendingUser);
	}

	@Override
	public Optional<PendingUser> findById(String pendingId) {
		PendingUser p = store.get(pendingId);
		if (p == null)
			return Optional.empty();
		if (p.expiresAt().isBefore(Instant.now())) {
			store.remove(pendingId);
			return Optional.empty();
		}
		return Optional.of(p);
	}

	@Override
	public void delete(String pendingId) {
		store.remove(pendingId);
	}
}
