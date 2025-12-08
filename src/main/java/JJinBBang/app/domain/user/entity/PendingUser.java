package JJinBBang.app.domain.user.entity;

import java.time.Instant;

import JJinBBang.app.global.common.enums.Provider;

public record PendingUser(String pendingId, Provider provider, String providerId, Instant expiresAt) {
}
