package JJinBBang.app.domain.user.repository;

import java.util.Optional;

import JJinBBang.app.domain.user.entity.PendingUser;

public interface PendingUserRepository {
	void save(PendingUser pendingUser);
	Optional<PendingUser> findById(String pendingId);
	void delete(String pendingId);
}
